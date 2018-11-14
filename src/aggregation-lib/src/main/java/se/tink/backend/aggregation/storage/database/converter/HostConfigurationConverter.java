package se.tink.backend.aggregation.storage.database.converter;

import se.tink.backend.aggregation.aggregationcontroller.v1.core.HostConfiguration;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.storage.database.models.ClusterConfiguration;

import java.util.Base64;

public class HostConfigurationConverter {

    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    public static HostConfiguration convert(ClusterInfo clusterInfo){
        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setClusterId(clusterInfo.getClusterId().getId());
        hostConfiguration.setHost(clusterInfo.getAggregationControllerHost());
        hostConfiguration.setApiToken(clusterInfo.getApiToken());
        hostConfiguration.setBase64encodedclientcert(BASE64_ENCODER.encodeToString(clusterInfo.getClientCertificate()));
        hostConfiguration.setDisablerequestcompression(clusterInfo.isDisableRequestCompression());
        return hostConfiguration;
    }

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
