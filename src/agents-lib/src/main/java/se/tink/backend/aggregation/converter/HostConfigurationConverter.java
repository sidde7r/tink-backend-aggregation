package se.tink.backend.aggregation.converter;

import se.tink.backend.aggregation.aggregationcontroller.v1.core.HostConfiguration;
import se.tink.backend.aggregation.api.CallbackHostConfiguration;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;

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

    public static HostConfiguration convert(CallbackHostConfiguration callbackHostConfiguration){
        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setClusterId(callbackHostConfiguration.getClusterId());
        hostConfiguration.setHost(callbackHostConfiguration.getHost());
        hostConfiguration.setApiToken(callbackHostConfiguration.getApiToken());
        hostConfiguration.setBase64encodedclientcert(callbackHostConfiguration.getBase64encodedclientcert());
        hostConfiguration.setDisablerequestcompression(callbackHostConfiguration.isDisablerequestcompression());
        return hostConfiguration;
    }
}
