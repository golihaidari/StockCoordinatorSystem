package dk.dtu;

import org.jspace.*;
import java.net.URI;

public class SpaceServer {
    public static void main(String[] args) throws Exception {
        SpaceRepository repository = new SpaceRepository();

        // Create remote spaces
        repository.add("requestChannel", new SequentialSpace());
        repository.add("responseChannel", new SequentialSpace());

        // Expose spaces to the network
        URI uri = new URI("tcp://localhost:9001/?keep");
        repository.addGate(uri.toString());

        System.out.println("[Space_Server] : is running at [" + uri+ "]");
    }
}
