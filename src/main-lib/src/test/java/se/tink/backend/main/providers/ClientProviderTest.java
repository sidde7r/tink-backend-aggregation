package se.tink.backend.main.providers;

import org.junit.Test;
import se.tink.backend.core.Client;
import se.tink.libraries.cluster.Cluster;

public class ClientProviderTest {

    @Test
    public void validateSEClients() {
        ClientProvider provider = new ClientProvider(Cluster.TINK);

        for (Client c : provider.get().values()) {
            c.validate();
        }
    }

    @Test
    public void validateAbnAmroClients() {
        ClientProvider provider = new ClientProvider(Cluster.ABNAMRO);

        for (Client c : provider.get().values()) {
            c.validate();
        }
    }

    @Test
    public void validateSEBClients() {
        ClientProvider provider = new ClientProvider(Cluster.CORNWALL);

        for (Client c : provider.get().values()) {
            c.validate();
        }
    }
}
