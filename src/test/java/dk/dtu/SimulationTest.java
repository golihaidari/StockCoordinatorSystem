package dk.dtu;

import org.jspace.RemoteSpace;

public class SimulationTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Starting System Simulation with Threads...");

        // Step 1: Connect to SpaceServer
        RemoteSpace requestChannel = new RemoteSpace("tcp://localhost:9001/requestChannel?keep");
        RemoteSpace responseChannel = new RemoteSpace("tcp://localhost:9001/responseChannel?keep");

        // Step 2: Create and run Store threads
        Thread store1Thread = new Thread(() -> {
            try {
                Store store1 = new Store("Store1", requestChannel, responseChannel);
                
                // Positive test: Request 20 Apples (should succeed)
                store1.sendRequest("Apples", 20, "Requesting");
                
                // Negative test: Request 500 Bananas (should fail due to lack of stock)
                store1.sendRequest("Bananas", 500, "Requesting");
                
                // Positive test: Restock 50 Apples (should succeed)
                store1.sendRequest("Apples", 50, "Restocking");
                
                // Negative test: Restock 1000 Oranges (should fail due to non-existent product)
                store1.sendRequest("Oranges", 1000, "Restocking");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread store2Thread = new Thread(() -> {
            try {
                Store store2 = new Store("Store2", requestChannel, responseChannel);
                
                // Positive test: Restock 50 Oranges (should succeed)
                store2.sendRequest("Oranges", 50, "Restocking");
                
                // Negative test: Request 200 Apples (should fail due to lack of stock)
                store2.sendRequest("Apples", 200, "Requesting");
                
                // Positive test: Restock 30 Apples (should succeed)
                store2.sendRequest("Apples", 30, "Restocking");
                
                // Negative test: Request 300 Oranges (should fail due to lack of stock)
                store2.sendRequest("Oranges", 300, "Requesting");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Step 3: Start threads
        store1Thread.start();
        store2Thread.start();

        // Step 4: Wait for threads to finish
        store1Thread.join();
        store2Thread.join();

        System.out.println("\nSystem Simulation Completed with Threads.");
    }
}
