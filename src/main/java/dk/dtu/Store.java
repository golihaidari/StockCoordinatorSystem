package dk.dtu;

import org.jspace.*;

import java.util.Scanner;
import java.util.UUID;

public class Store {
    private final String name;
    private final Space requestChannel;
    private final Space responseChannel;

    public Store(String name, Space requestChannel, Space responseChannel) {
        this.name = name;
        this.requestChannel = requestChannel;
        this.responseChannel = responseChannel;
    }

    public void sendRequest(String product, int quantity, String command) {
        try {
            // Generate a unique request ID for each request
            String requestId = UUID.randomUUID().toString();
    
            System.out.println("[" + name + "] " + command + " " + quantity + " of " + product + " with RequestID: " + requestId);
    
            // Send a request to the ResourceManager with the requestId
            requestChannel.put(name, product, quantity, command, requestId);
    
            // Get the response from ResourceManager, looking for the correct RequestID
            Object[] response = responseChannel.get(
                new FormalField(String.class),  // Store name
                new FormalField(String.class),  // Product name
                new FormalField(String.class),  // Response (approved/denied)
                new FormalField(String.class)   // Request ID
            );
    
            // Ensure that the response corresponds to the correct RequestID
            String responseMessage = (String) response[2];
            String responseRequestId = (String) response[3];
    
            if (responseRequestId.equals(requestId)) {
                System.out.println("[" + name + "] Response for RequestID: " + responseRequestId + " - " + responseMessage);
            } else {
                System.out.println("[" + name + "] Error: Mismatched RequestID. Expected: " + requestId + " but got: " + responseRequestId);
            }
    
        } catch (InterruptedException e) {
            e.printStackTrace();
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

    public static void main(String[] args) throws Exception {
        RemoteSpace requestChannel = new RemoteSpace("tcp://localhost:9001/requestChannel?keep");
        RemoteSpace responseChannel = new RemoteSpace("tcp://localhost:9001/responseChannel?keep");
        Store store = new Store("Store1", requestChannel, responseChannel);
        store.run();
    }
}
