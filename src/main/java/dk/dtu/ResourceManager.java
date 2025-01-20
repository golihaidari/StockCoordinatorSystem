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
        System.out.println("[Resource_Manager] running...");
    }

    public void processRequests() {
        new Thread(() -> {
            try {
                while (true) {
                    // Wait and receive a request from Store
                    Object[] request = requestChannel.get(
                        new FormalField(String.class),  // Store name
                        new FormalField(String.class),  // Product name
                        new FormalField(Integer.class), // Quantity
                        new FormalField(String.class),  // Command (Requesting/Restocking)
                        new FormalField(String.class)   // Request ID
                    );

                    String storeName = (String) request[0];
                    String product = (String) request[1];
                    int quantity = (Integer) request[2];
                    String command = (String) request[3];
                    String requestId = (String) request[4]; // Extract requestId

                    System.out.println("[ResourceManager] <- GET:  request("+ storeName +", "+ product + ", "+ quantity +", "+ command+ ", " +requestId +").");
                    printProduct(storeName, product);
                    
                    boolean success = false;

                    System.out.print("@[ResourceManager] start " + command + "...");
                    // Process the request based on the command: allocation or restocking
                    switch (command){
                        case "Requesting" -> success = checkAndAllocateStock(storeName, product, quantity);
                        case "Restocking" -> success = restockProduct(storeName, product, quantity);  
                        default ->  System.out.println("[ResourceManager] : " + command + " is and invalid command.");
                    }
                    
                    printProduct(storeName, product);

                    System.out.println("[ResourceManager] -> SEND : response("+ storeName+ ", "+ product +", "+(success ? "Approved" : "Denied") + "," + requestId+")");
                    // Send the response back to the Store with the requestId
                    responseChannel.put(storeName, product, success ? "Approved" : "Denied", requestId);
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
                System.out.println(" Allocation Succeed : (" + store + ", " + product + ", " + quantity+")");
                return true;
            } else {
                productSpace.put(store, product, currentStock);
                System.out.println(" Allocation Faild : Due to not  enough "+ product + " at "+ store+". (requestQuantity " + quantity + " > currentStock " + currentStock +")");
                return false;
            }
        } else {
            System.out.println(" Allocation Faild : Due to not existance of " + product + " at " +store);
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
            
            System.out.println(" Restoking Succeed : Restocked(" + quantity + " " + product + " at " + store+ ")");
        } else {
            productSpace.put(store, product, quantity);
            System.out.println(" Restoking Succeed : Created(" + quantity + " " + product + " at" + store+")");
        }
       
        return true;
    }

    private void printProduct(String storeName, String product) throws InterruptedException {
        Object[] productTuple = productSpace.queryp(
                new ActualField(storeName),
                new ActualField(product),
                new FormalField(Integer.class)
            );
        int currentStock = (Integer) productTuple[2];
        
        System.out.println("@[ResourceManager] Current ProductState (" + storeName + ", " + product + ", "+ currentStock +")");
    }

    public static void main(String[] args) throws Exception {
        RemoteSpace requestChannel = new RemoteSpace("tcp://localhost:9001/requestChannel?keep");
        RemoteSpace responseChannel = new RemoteSpace("tcp://localhost:9001/responseChannel?keep");

        ResourceManager resourceManager = new ResourceManager(requestChannel, responseChannel);
        resourceManager.processRequests();
    }
}
