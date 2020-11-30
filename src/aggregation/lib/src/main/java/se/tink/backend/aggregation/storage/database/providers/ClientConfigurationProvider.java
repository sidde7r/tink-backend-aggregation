package se.tink.backend.aggregation.storage.database.providers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Map;
import se.tink.backend.aggregation.cluster.exceptions.ClientNotValid;
import se.tink.backend.aggregation.cluster.exceptions.ClusterNotValid;
import se.tink.backend.aggregation.storage.database.models.ClientConfiguration;

public class ClientConfigurationProvider {

    private final Map<String, ClientConfiguration> clientConfigurationsByClientKey;
    // introduce a map that use client name (which is in db that is the same as cluster-id)
    private final Map<String, ClientConfiguration> clientConfigurationsByName;

    @Inject
    ClientConfigurationProvider(
            @Named("clientConfigurationByClientKey")
                    Map<String, ClientConfiguration> clientConfigurationsByClientKey,
            @Named("clientConfigurationByName")
                    Map<String, ClientConfiguration> clientConfigurationsByName) {
        this.clientConfigurationsByClientKey = clientConfigurationsByClientKey;
        this.clientConfigurationsByName = clientConfigurationsByName;
    }

    private boolean isValidClientName(String clusterId) {
        return clientConfigurationsByName.containsKey(clusterId);
    }

    private boolean isValidClientKey(String apiClientKey) {
        return clientConfigurationsByClientKey.containsKey(apiClientKey);
    }

    public ClientConfiguration getClientConfiguration(String apiKey) throws ClientNotValid {

        if (!isValidClientKey(apiKey)) {
            throw new ClientNotValid();
        }

        return clientConfigurationsByClientKey.get(apiKey);
    }

    public ClientConfiguration getClientConfiguration(String name, String environment)
            throws ClusterNotValid {

        String clusterId = String.format("%s-%s", name, environment);

        if (!isValidClientName(clusterId)) {
            throw new ClusterNotValid();
        }

        return clientConfigurationsByName.get(clusterId);
    }
}
