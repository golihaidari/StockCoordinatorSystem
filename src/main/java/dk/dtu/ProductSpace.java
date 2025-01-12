package dk.dtu;

import org.jspace.*;

public class ProductSpace{

    private static Space productSpace;

    // Private constructor to prevent instantiation
    private ProductSpace() {}

    public static Space getInstance() {
        if (productSpace == null) {
            synchronized (ProductSpace.class) {
                if (productSpace == null) {
                    try {
                        productSpace = new SequentialSpace();
                        initializeProducts();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return productSpace;
    }

    //Initialize the product space with dummy products.
    private static void initializeProducts() throws Exception {
        productSpace.put("Store1", "Apples", 100);
        productSpace.put("Store1", "Oranges", 50);
        productSpace.put("Store2", "Apples", 90);
        productSpace.put("Store2", "Oranges", 70);
        productSpace.put("Store3", "Apples", 10);
        productSpace.put("Store3", "Oranges", 25);
        productSpace.put("Warehouse1", "Apples", 600);
        productSpace.put("Warehouse1", "Oranges", 300);
        productSpace.put("Warehouse2", "Apples", 400);
        productSpace.put("Warehouse2", "Oranges", 300);
        System.out.println("ProductSpace created and initialized with dummy products.");
    }
}
