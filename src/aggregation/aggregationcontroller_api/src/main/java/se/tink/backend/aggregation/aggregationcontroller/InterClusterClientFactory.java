package se.tink.backend.aggregation.aggregationcontroller;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import se.tink.backend.aggregation.storage.database.models.ClusterConfiguration;
import se.tink.libraries.jersey.utils.ClientLoggingFilter;
import se.tink.libraries.jersey.utils.JerseyUtils;

public class InterClusterClientFactory {

    private static final String EMPTY_PASSWORD = "";

    private final ClientConfig clientConfig;
    private final Map<String, ClusterConfiguration> clusterConfigurations;

    @Inject
    public InterClusterClientFactory(
            ClientConfig clientConfig,
            @Named("clusterConfigurations")
                    Map<String, ClusterConfiguration> clusterConfigurations) {
        this.clientConfig = clientConfig;
        this.clusterConfigurations = clusterConfigurations;
    }

    public Client create(String clusterId) {
        ClusterConfiguration clusterConfiguration = clusterConfigurations.get(clusterId);

        if (clusterConfiguration == null) {
            throw new IllegalStateException(
                    String.format("Cluster configuration for clusterId: %s not found", clusterId));
        }

        Client client =
                JerseyUtils.getClusterClient(
                        clusterConfiguration.getClientCert(),
                        EMPTY_PASSWORD,
                        clusterConfiguration.isDisablerequestcompression(),
                        clientConfig);
        client.addFilter(new ClientLoggingFilter());
        JerseyUtils.registerAPIAccessToken(client, clusterConfiguration.getApiToken());
        return client;
    }
}
