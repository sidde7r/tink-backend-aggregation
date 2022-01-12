package se.tink.backend.aggregation.aggregationcontroller;

import com.sun.jersey.api.client.Client;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;

public class InterClusterClientProvider {

    private final InterClusterClientFactory factory;
    private final Map<String, Client> clientByClusterId = new ConcurrentHashMap<>();

    @Inject
    public InterClusterClientProvider(InterClusterClientFactory factory) {
        this.factory = factory;
    }

    public Client getByClusterId(String clusterId) {
        return clientByClusterId.computeIfAbsent(clusterId, factory::create);
    }
}
