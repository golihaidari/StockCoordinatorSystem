package dk.dtu;

import org.jspace.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResourceManager {
    private final Space requestChannel;
    private final Space responseChannel;
    private final Space productSpace;
    private final TokenManager tokenManager;
    private final ExecutorService executorService;

    public ResourceManager(Space requestChannel, Space responseChannel) {
        this.requestChannel = requestChannel;
        this.responseChannel = responseChannel;
        this.productSpace = ProductSpace.getInstance(); // Singleton pattern
        this.tokenManager = TokenManager.getInstance(); // Singleton pattern
        this.executorService = Executors.newFixedThreadPool(10); // Thread pool for efficiency
        System.out.println("[ResourceManager] Running...");
    }

    public void processRequests() {
        new Thread(() -> {
            try {
                while (true) {
                    Object[] request = requestChannel.get(
                        new FormalField(String.class), // Store name
                        new FormalField(String.class), // Product name
                        new FormalField(Integer.class), // Quantity
                        new FormalField(String.class), // Command
                        new FormalField(String.class)  // Request ID
                    );

                    executorService.submit(() -> handleRequest(request));
                }
            } catch (InterruptedException e) {
                System.err.println("[ResourceManager] Error: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void handleRequest(Object[] request) {
        String storeName = (String) request[0];
        String product = (String) request[1];
        int quantity = (Integer) request[2];
        String command = (String) request[3];
        String requestId = (String) request[4];

        System.out.println("[ResourceManager][Thread-" + Thread.currentThread().threadId() + "] <- GET: request(" + storeName + ", " + product + ", " + quantity + ", " + command + ", " + requestId + ").");
        try {
            tokenManager.acquireToken(storeName, product); // Acquire token
            try {
                boolean success = false;
                System.out.println("@[ResourceManager][Thread-" + Thread.currentThread().threadId() + "] start " + command + "...");

                switch (command) {
                    case "Requesting" -> success = checkAndAllocateStock(storeName, product, quantity);
                    case "Restocking" -> success = restockProduct(storeName, product, quantity);
                    default -> System.out.println("[ResourceManager] Invalid command: " + command);
                }

                printProduct(storeName, product);
                System.out.println("[ResourceManager][Thread-" + Thread.currentThread().threadId() + "] -> SEND: response(" + storeName + ", " + product + ", " + (success ? "Approved" : "Denied") + ", " + requestId + ")");
                responseChannel.put(storeName, product, success ? "Approved" : "Denied", requestId);
            } finally {
                tokenManager.releaseToken(storeName, product); // Release token
            }
        } catch (InterruptedException e) {
            System.err.println("[ResourceManager] Error: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private boolean checkAndAllocateStock(String store, String product, int quantity) throws InterruptedException {
        Object[] productTuple = productSpace.queryp(
            new ActualField(store),
            new ActualField(product),
            new FormalField(Integer.class)
        );

        if (productTuple != null) {
            Object[] obj = productSpace.get(
                new ActualField(store),
                new ActualField(product),
                new FormalField(Integer.class)
            );
            int currentStock = (Integer) obj[2];

            if (currentStock >= quantity) {
                productSpace.put(store, product, currentStock - quantity);
                System.out.println("@[ResourceManager][Thread-" + Thread.currentThread().threadId() + "]Allocation Succeed: (" + store + ", " + product + ", " + quantity + ")");
                return true;
            } else {
                productSpace.put(store, product, currentStock);
                System.out.println("@[ResourceManager][Thread-" + Thread.currentThread().threadId() + "]Allocation Failed: Not enough " + product + " at " + store + ". (Requested: " + quantity + ", Available: " + currentStock + ")");
                return false;
            }
        } else {
            System.out.println("@[ResourceManager][Thread-" + Thread.currentThread().threadId() + "]Allocation Failed: Product " + product + " does not exist at " + store);
            return false;
        }
    }

    private boolean restockProduct(String store, String product, int quantity) throws InterruptedException {
        Object[] productTuple = productSpace.queryp(
            new ActualField(store),
            new ActualField(product),
            new FormalField(Integer.class)
        );

        if (productTuple != null) {
            Object[] obj = productSpace.get(
                new ActualField(store),
                new ActualField(product),
                new FormalField(Integer.class)
            );
            int currentStock = (Integer) obj[2];
            productSpace.put(store, product, currentStock + quantity);
            System.out.println("@[ResourceManager][Thread-" + Thread.currentThread().threadId() + "] Restocking Succeed: Restocked(" + quantity + " " + product + " at " + store + ")");
        } else {
            productSpace.put(store, product, quantity);
            System.out.println("@[ResourceManager][Thread-" + Thread.currentThread().threadId() + "]Restocking Succeed: Created(" + quantity + " " + product + " at " + store + ")");
        }

        return true;
    }

    private void printProduct(String storeName, String product) throws InterruptedException {
        Object[] productTuple = productSpace.queryp(
            new ActualField(storeName),
            new ActualField(product),
            new FormalField(Integer.class)
        );

        if (productTuple != null) {
            int currentStock = (Integer) productTuple[2];
            System.out.println("@[ResourceManager][Thread-" + Thread.currentThread().threadId() + "] Current ProductState (" + storeName + ", " + product + ", " + currentStock + ")");
        } else {
            System.out.println("@[ResourceManager][Thread-" + Thread.currentThread().threadId() + "] Product (" + storeName + ", " + product + ") does not exist in the product space.");
        }
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        RemoteSpace requestChannel = new RemoteSpace("tcp://localhost:9001/requestChannel?keep");
        RemoteSpace responseChannel = new RemoteSpace("tcp://localhost:9001/responseChannel?keep");

        ResourceManager resourceManager = new ResourceManager(requestChannel, responseChannel);
        resourceManager.processRequests();
    }
}
