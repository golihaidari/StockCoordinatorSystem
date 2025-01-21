package dk.dtu;

import org.jspace.*;
import java.net.URI;
import java.net.URISyntaxException;

public class SpaceServer {
    public static void main(String[] args) {
        SpaceRepository repository = new SpaceRepository();

        // Create remote spaces
        repository.add("requestChannel", new SequentialSpace());
        repository.add("responseChannel", new SequentialSpace());

        try {
            // Expose spaces to the network
            URI uri = new URI("tcp://localhost:9001/?keep");
            repository.addGate(uri.toString());

            System.out.println("[Space_Server] : is running at [" + uri+ "]");
        } catch (URISyntaxException e) {
            System.err.println(e.getStackTrace());
        }  
    }
}
