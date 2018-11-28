package se.tink.backend.aggregation.storage.database.converter;

import se.tink.backend.aggregation.aggregationcontroller.v1.core.HostConfiguration;
import se.tink.backend.aggregation.storage.database.models.ClusterConfiguration;

public class HostConfigurationConverter {
    public static HostConfiguration convert(ClusterConfiguration clusterConfiguration) {
        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setClusterId(clusterConfiguration.getClusterId());
        hostConfiguration.setHost(clusterConfiguration.getHost());
        hostConfiguration.setApiToken(clusterConfiguration.getApiToken());
        hostConfiguration.setBase64encodedclientcert(clusterConfiguration.getBase64encodedclientcert());
        hostConfiguration.setDisablerequestcompression(clusterConfiguration.isDisablerequestcompression());
        return hostConfiguration;
    }
}
