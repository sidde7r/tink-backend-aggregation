package se.tink.backend.aggregation.storage.database.providers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import se.tink.backend.aggregation.storage.database.models.ClientConfiguration;

import java.util.Map;

public class ClientConfigurationProvider {

    private final Map<String, ClientConfiguration> clientConfigurations;

    @Inject
    ClientConfigurationProvider(@Named("clientConfiguration") Map<String, ClientConfiguration> clientConfigurations) {
        this.clientConfigurations = clientConfigurations;
    }

    // at this moment we only validate the apiClientKey is in the database, we do not check if other keys under this
    // apiClientKeys are valid keys.
    public boolean isValid(String apiClientKey) {
        return clientConfigurations.containsKey(apiClientKey);
    }

    public ClientConfiguration getClientConfiguration(String apiClientKey) {
        if (!isValid(apiClientKey)) {
            // returning null at the moment, but later we should throw an exception ideally
            return null;
        }
        return clientConfigurations.get(apiClientKey);
    }
}
