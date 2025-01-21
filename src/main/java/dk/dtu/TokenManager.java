package dk.dtu;

import org.jspace.*;

public class TokenManager {

    private static TokenManager instance; // Singleton instance
    private final Space tokenSpace;      // Shared token space

    private TokenManager() {
        this.tokenSpace = new SequentialSpace();
    }

    // Singleton instance getter
    public static synchronized TokenManager getInstance() {
        if (instance == null) {
            instance = new TokenManager();
        }
        return instance;
    }

    // Acquire token for a specific store-product pair
    public void acquireToken(String store, String product) throws InterruptedException {
        String tokenKey = store + ":" + product;

        // Atomically initialize the token if it doesn't exist
        synchronized (tokenSpace) {
            if (tokenSpace.queryp(new ActualField(tokenKey), new FormalField(Integer.class)) == null) {
                tokenSpace.put(tokenKey, 1); // Token initialized to '1' (unlocked)
            }
        }

        // Block until the token is available
        tokenSpace.get(new ActualField(tokenKey), new ActualField(1)); // Acquire token
        tokenSpace.put(tokenKey, 0); // Set token to '0' (locked)
    }

    // Release token for a specific store-product pair
    public void releaseToken(String store, String product) throws InterruptedException {
        String tokenKey = store + ":" + product;
        tokenSpace.put(tokenKey, 1); // Set token back to '1' (unlocked)
    }
}
