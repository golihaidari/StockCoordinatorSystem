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
        productSpace.put("Store1", "Apples", 10);
        productSpace.put("Store2", "Apples", 10);
        productSpace.put("Store3", "Apples", 10);
        
        productSpace.put("Store1", "Oranges", 20);
        productSpace.put("Store2", "Oranges", 20);
        productSpace.put("Store3", "Oranges", 20);

        System.out.println("@[ProductSpace] Dummy data is intialized:");
        printData();
    }

    public static void printData(){
        try {
            Iterable<Object[]> products = productSpace.queryAll(
                new FormalField(String.class), // Store name
                new FormalField(String.class), // Product name
                new FormalField(Integer.class) // Quantity
            );

            for (Object[] product : products) {
                System.out.println("    #" + product[0] + ", "+ product[1] + ", "+  product[2]);
            }
        } catch (InterruptedException e) {
            System.err.println("@[ProductSpace] Error printing product space data.");
            e.printStackTrace();
        }
    }
}
