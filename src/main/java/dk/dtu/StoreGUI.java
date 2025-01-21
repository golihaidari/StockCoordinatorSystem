package dk.dtu;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.jspace.RemoteSpace;

public class StoreGUI extends Application {
    private final Store store; // Store instance for backend communication
    private static String storeName; // Static variable to hold the store name


    public StoreGUI() throws Exception {
        // Initialize the Store object with communication channels
        RemoteSpace requestChannel = new RemoteSpace("tcp://localhost:9001/requestChannel?keep");
        RemoteSpace responseChannel = new RemoteSpace("tcp://localhost:9001/responseChannel?keep");
        store = new Store(storeName, requestChannel, responseChannel);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle(storeName);

        // Product dropdowns
        ComboBox<String> productDropdown = new ComboBox<>();
        productDropdown.getItems().addAll("Apples", "Oranges"); // Add more products dynamically if possible
        productDropdown.setPromptText("Select Product");

        //Quantity dropdowns
        ComboBox<Integer> quantityDropdown = new ComboBox<>();
        quantityDropdown.getItems().addAll(1, 5, 10, 20, 50); // Add more quantities dynamically if needed
        quantityDropdown.setPromptText("Select Quantity");

        // Action Buttons
        Button requestStockButton = new Button("Request Stock");
        Button restockButton = new Button("Restock");

        // Log Area
        TextArea logArea = new TextArea();
        logArea.setEditable(false);

        // Button Actions
        requestStockButton.setOnAction(event -> {
            String product = productDropdown.getValue();
            Integer quantity = quantityDropdown.getValue();

            if (product != null && quantity != null) {
                String response = store.sendRequest(product, quantity, "Requesting");
                logArea.appendText("Request sent: " + product + ", Quantity: " + quantity + "\n");
                logArea.appendText("Response received: " + response + "\n");
            } else {
                showAlert("Please select both a product and a quantity.");
            }
        });

        restockButton.setOnAction(event -> {
            String product = productDropdown.getValue();
            Integer quantity = quantityDropdown.getValue();

            if (product != null && quantity != null) {
                String response = store.sendRequest(product, quantity, "Restocking");
                logArea.appendText("Restock sent: " + product + ", Quantity: " + quantity + "\n");
                logArea.appendText("Response: " + response + "\n");
            } else {
                showAlert("Please select both a product and a quantity.");
            }
        });

        // Layout
        GridPane layout = new GridPane();
        layout.setVgap(10);
        layout.setHgap(10);
        layout.setPadding(new Insets(15)); // Add space from the left and other sides

        layout.add(new Label("Product:"), 0, 0);
        layout.add(productDropdown, 1, 0);
        layout.add(new Label("Quantity:"), 0, 1);
        layout.add(quantityDropdown, 1, 1);
        layout.add(new Label("Action:"), 0, 2);
        layout.add(requestStockButton, 1, 2);
        layout.add(restockButton, 2, 2); // Keep the buttons closer by reducing space in their layout

        layout.add(new Label("Log:"), 0, 3);
        layout.add(logArea, 0, 4, 3, 1); // Make the log span three columns for space utilization

        primaryStage.setScene(new Scene(layout, 400, 300));
        primaryStage.show();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Input Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        storeName = (args.length > 0) ? args[0] : "Store1";

        launch(args);
    }
}
