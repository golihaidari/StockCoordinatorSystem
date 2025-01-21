package dk.dtu;

import org.jspace.RemoteSpace;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

// SpaceServer and REsourceManager are reuired to be running, before runs this test class
public class SystemFunctionalityTest {

    private RemoteSpace requestChannel;
    private RemoteSpace responseChannel;

    @Before
    public void setUp() throws Exception {
        // Setup: Connect to SpaceServer and create channels
        requestChannel = new RemoteSpace("tcp://localhost:9001/requestChannel?keep");
        responseChannel = new RemoteSpace("tcp://localhost:9001/responseChannel?keep");
    }

    @Test
    public void testRestockingRequest() throws Exception {
        Store store = new Store("Store1", requestChannel, responseChannel);

        // Simulate Restocking Request
        String response = store.sendRequest("Apples", 10, "Restocking");

        // Check if the result is "Approved"
        assertEquals("Restocking response should be approved.", "Approved", response);
    }

    @Test
    public void testStockRequestSuccess() throws Exception {
        Store store = new Store("Store2", requestChannel, responseChannel);

        // Add some stock first
        store.sendRequest("Oranges", 15, "Restocking");

        // Request stock
        String response = store.sendRequest("Oranges", 10, "Requesting");

        // Check if the response is "Approved"
        assertEquals("Stock request should be approved.", "Approved", response);
    }

    @Test
    public void testStockRequestFailure() throws Exception {
        Store store = new Store("Store3", requestChannel, responseChannel);

        // Request non-existent stock
        String response = store.sendRequest("Bananas", 5, "Requesting");

        // Check if the response is "Denied"
        assertEquals("Requesting non-existent stock should be denied.", "Denied", response);
    }

    @Test
    public void testInvalidRequest() throws Exception {
        Store store = new Store("Store4", requestChannel, responseChannel);

        // Request 100 quantity (invalid)
        String response = store.sendRequest("Apples", 100, "Requesting");

        // Check if the result is "Denied"
        assertEquals("Requesting zero stock should be denied.", "Denied", response);
    }
}
