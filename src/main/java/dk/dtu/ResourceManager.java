package dk.dtu;

import org.jspace.*;

public class ResourceManager {
    private final Space requestChannel;
    private final Space responseChannel;
    private final Space productSpace;

    public ResourceManager(Space requestChannel, Space responseChannel) {
        this.requestChannel = requestChannel;
        this.responseChannel = responseChannel;
        this.productSpace = ProductSpace.getInstance();  // Singleton pattern for product space
    }

    public void processRequests() {
        new Thread(() -> {
            try {
                while (true) {
                    // Receive a request from Store
                    Object[] request = requestChannel.get(
                        new FormalField(String.class),  // Store name
                        new FormalField(String.class),  // Product name
                        new FormalField(Integer.class), // Quantity
                        new FormalField(String.class),  // Command (Requesting/Restocking)
                        new FormalField(String.class)   // Request ID
                    );

                    String store = (String) request[0];
                    String product = (String) request[1];
                    int quantity = (Integer) request[2];
                    String command = (String) request[3];
                    String requestId = (String) request[4]; // Extract requestId

                    boolean success = false;

                    // Process the request based on the command
                    switch (command) {
                        case "Requesting":
                            success = checkAndAllocateStock(store, product, quantity);
                            break;
                        case "Restocking":
                            success = restockProduct(store, product, quantity);
                            break;
                        default:
                            System.out.println("[ResourceManager] Unknown command: " + command);
                    }

                    // Send the response back to the Store with the requestId
                    responseChannel.put(store, product, success ? "Approved" : "Denied", requestId);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private synchronized boolean checkAndAllocateStock(String store, String product, int quantity) throws InterruptedException {
        Object[] productTuple = productSpace.getp(
            new ActualField(store), 
            new ActualField(product), 
            new FormalField(Integer.class)
        );
    
        if (productTuple != null) {
            int currentStock = (Integer) productTuple[2];
            if (currentStock >= quantity) {
                productSpace.put(store, product, currentStock - quantity);
                System.out.println("[ResourceManager] Allocated " + quantity + " of " + product);
                return true;
            } else {
                productSpace.put(store, product, currentStock);
                System.out.println("[ResourceManager] Not enough stock for " + product);
                return false;
            }
        } else {
            System.out.println("[ResourceManager] Product " + product + " does not exist.");
            return false;
        }
    }

    public synchronized boolean restockProduct(String store, String product, int quantity) throws InterruptedException {
        Object[] productTuple = productSpace.queryp(
            new ActualField(store), 
            new ActualField(product), 
            new FormalField(Integer.class)
        );

        if (productTuple != null) {
            int currentStock = (Integer) productTuple[2];
            productSpace.get(new ActualField(store), new ActualField(product), new FormalField(Integer.class));
            productSpace.put(store, product, currentStock + quantity);
        } else {
            productSpace.put(store, product, quantity);
        }
        System.out.println("[ResourceManager] Restocked " + quantity + " of " + product);
        return true;
    }

    public static void main(String[] args) throws Exception {
        RemoteSpace requestChannel = new RemoteSpace("tcp://localhost:9001/requestChannel?keep");
        RemoteSpace responseChannel = new RemoteSpace("tcp://localhost:9001/responseChannel?keep");

        ResourceManager resourceManager = new ResourceManager(requestChannel, responseChannel);
        resourceManager.processRequests();

        System.out.println("ResourceManager is running and processing requests.");
    }
}
