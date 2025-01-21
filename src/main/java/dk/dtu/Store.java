package dk.dtu;

import org.jspace.*;

import java.util.Scanner;
import java.util.UUID;

public class Store {
    public final String name;
    private final Space requestChannel;
    private final Space responseChannel;

    public Store(String name, Space requestChannel, Space responseChannel) {
        this.name = name;
        this.requestChannel = requestChannel;
        this.responseChannel = responseChannel;
    }

    public String sendRequest(String product, int quantity, String command) {
        try {
            // Generate a unique request ID for each request
            String requestId = UUID.randomUUID().toString().substring(0,4);
    
            System.out.println("[" + name + "] -> SEND:  request("+ product + ", "+ quantity +", "+ command+ ", " +requestId +").");
       
            // Send a request to the ResourceManager with the requestId
            requestChannel.put(name, product, quantity, command, requestId);
    
            // Wait and get the response from ResourceManager, looking for the correct RequestID
            Object[] response = responseChannel.get(
                new FormalField(String.class),  // Store name
                new FormalField(String.class),  // Product name
                new FormalField(String.class),  // Response (approved/denied)
                new ActualField(requestId)   // Request ID to ensure retreiving the corresponded response 
            );
    
            // 
            String rName = (String) response[0];
            String rProduct = (String) response[1];
            String rStatus = (String) response[2];
            String responseRequestId = (String) response[3];

            // Format the response message
            String responseMessage = "[" + name + "] <- GET: response(" + rName + ", " + rProduct + ", " + rStatus + ", " + responseRequestId + ")";

            // Ensure that the response corresponds to the correct RequestID
            if (!responseRequestId.equals(requestId)) {
                responseMessage += "\n@[Store] Warning: Mismatched RequestID. Expected: " + requestId + ", but got: " + responseRequestId;
            }

            System.out.println(responseMessage);

            return rStatus; 

        } catch (InterruptedException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    
    public void run() throws Exception {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nChoose an option:");
            System.out.println("1 - Request stock");
            System.out.println("2 - Restock");
            System.out.println("3 - Exit");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (choice == 3) {
                System.out.println("Exiting...");
                break;
            }

            System.out.print("Enter product name: ");
            String product = scanner.nextLine();

            System.out.print("Enter quantity: ");
            int quantity = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    sendRequest(product, quantity, "Requesting");
                    break;
                case 2:
                    sendRequest(product, quantity, "Restocking");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
        scanner.close();
    }

    public static void main(String[] args) throws Exception{
        RemoteSpace requestChannel = new RemoteSpace("tcp://localhost:9001/requestChannel?keep");
        RemoteSpace responseChannel = new RemoteSpace("tcp://localhost:9001/responseChannel?keep");
        Store store = new Store("Store1", requestChannel, responseChannel);
        store.run();
    }
}
