package se.tink.backend.aggregation.storage.database.providers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.storage.database.models.ClientConfiguration;

import java.util.Map;

public class ClientConfigurationProvider {

    private final Map<String, ClientConfiguration> clientConfigurationsByClientKey;
    // introduce a map that use client name (which is in db that is the same as cluster-id)
    private final Map<String, ClientConfiguration> clientConfigurationsByName;

    private static final Logger log = LoggerFactory.getLogger(ClientConfigurationProvider.class);

    @Inject
    ClientConfigurationProvider(
            @Named("clientConfigurationByClientKey") Map<String, ClientConfiguration> clientConfigurationsByClientKey,
            @Named("clientConfigurationByName") Map<String, ClientConfiguration> clientConfigurationsByName) {
        this.clientConfigurationsByClientKey = clientConfigurationsByClientKey;
        this.clientConfigurationsByName = clientConfigurationsByName;
    }

    public boolean isValidName(String clusterId) {
        return clientConfigurationsByName.containsKey(clusterId);
    }

    public boolean isValidClientKey(String apiClientKey) {
        return clientConfigurationsByClientKey.containsKey(apiClientKey);
    }

    public boolean isValid(String identifier) {
        return isValidName(identifier) || isValidClientKey(identifier);
    }

    public ClientConfiguration getClientConfiguration(String identifier) {

        // handles the multi client solution where we have client id in header
        if (isValidClientKey(identifier)) {
            return clientConfigurationsByClientKey.get(identifier);
        }

        // handles the non multi client solution where we still use cluster id to identify the source,
        // this identifier is the clusterid, which we use as client name at the moment.
        if (isValidName(identifier)) {
            return clientConfigurationsByName.get(identifier);
        }

        // returning null at the moment, but later we should throw an exception ideally
        log.error("cannot find information for identifier {}, which is neither a valid client key or client name",
                identifier);
        return null;
    }
}
