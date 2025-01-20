package dk.dtu;

import org.jspace.RemoteSpace;

public class IntegrationTest {

    public static void main(String[] args) throws Exception {
        // Create channels
        RemoteSpace requestChannel = new RemoteSpace("tcp://localhost:9001/requestChannel?keep");
        RemoteSpace responseChannel = new RemoteSpace("tcp://localhost:9001/responseChannel?keep");

        // Initialize stores
        Store store1 = new Store("Store1", requestChannel, responseChannel);
        Store store2 = new Store("Store2", requestChannel, responseChannel);

        // Initialize ResourceManager
        ResourceManager resourceManager = new ResourceManager(requestChannel, responseChannel);
        resourceManager.processRequests();

        // Run integration tests
        runTest1(store1, store2);
        runTest2(store1);
    }

    private static void runTest1(Store store1, Store store2) throws InterruptedException {
        System.out.println("\n--- Test 1: Asynchronous Communication and Concurrent Requests ---");

        // Thread 1: Store1 requests stock
        Thread t1 = new Thread(() -> {
            store1.sendRequest("Apples", 3, "Requesting");
        });

        // Thread 2: Store2 restocks the same product
        Thread t2 = new Thread(() -> {
            store2.sendRequest("Apples", 3, "Restocking");
        });

        // Start threads
        t1.start();
        t2.start();

        // Wait for threads to complete
        t1.join();
        t2.join();
    }

    private static void runTest2(Store store1) throws InterruptedException {
        System.out.println("\n--- Test 2: Shared Resource Access with Token Synchronization ---");

        // Thread 1: Store1 requests stock (should succeed)
        Thread t1 = new Thread(() -> {
            store1.sendRequest("Oranges", 5, "Requesting");
        });

        // Thread 2: Store1 requests stock again (should fail due to insufficient stock)
        Thread t2 = new Thread(() -> {
            store1.sendRequest("Oranges", 20, "Requesting");
        });

        // Start threads
        t1.start();
        t2.start();

        // Wait for threads to complete
        t1.join();
        t2.join();
    }
}
