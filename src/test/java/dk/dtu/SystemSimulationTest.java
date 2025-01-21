package dk.dtu;

import org.jspace.RemoteSpace;

// SpaceServer and REsourceManager are reuired to be running, before runs this test class
public class SystemSimulationTest {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting System Simulation Test...");

        // Setup: Connect to SpaceServer and create channels
        RemoteSpace requestChannel = new RemoteSpace("tcp://localhost:9001/requestChannel?keep");
        RemoteSpace responseChannel = new RemoteSpace("tcp://localhost:9001/responseChannel?keep");

        // Create Store threads
        Thread store1Thread = new Thread(() -> simulateStore("Store1", requestChannel, responseChannel));
        Thread store2Thread = new Thread(() -> simulateStore("Store2", requestChannel, responseChannel));
        Thread store3Thread = new Thread(() -> simulateStore("Store3", requestChannel, responseChannel));
        Thread store4Thread = new Thread(() -> simulateStore("Store4", requestChannel, responseChannel));

        // Start Store threads
        store1Thread.start();
        store2Thread.start();
        store3Thread.start();
        store4Thread.start();

        // Wait for all threads to finish
        store1Thread.join();
        store2Thread.join();
        store3Thread.join();
        store4Thread.join();

        System.out.println("\nSystem Simulation Test Completed.");
    }


    private static void simulateStore(String storeName, RemoteSpace requestChannel, RemoteSpace responseChannel) {
        try {
            Store store = new Store(storeName, requestChannel, responseChannel);

            // Positive Tests
            store.sendRequest("Apples", 10, "Restocking");  // Add stock
            store.sendRequest("Apples", 5, "Requesting");   // Request stock (should succeed)
            store.sendRequest("Oranges", 15, "Restocking"); // Add stock

            // Negative Tests
            store.sendRequest("Bananas", 5, "Requesting");  // Request non-existent stock (should fail)
            store.sendRequest("Apples", 100, "Requesting"); // Request more than available stock (should fail)
            store.sendRequest("Oranges", 0, "Requesting");  // Request zero stock (invalid)

            // Additional Scenarios
            store.sendRequest("Bananas", 10, "Restocking"); // Create a new product
            store.sendRequest("Bananas", 5, "Requesting");  // Request newly created product (should succeed)
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
