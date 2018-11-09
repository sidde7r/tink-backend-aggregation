package se.tink.backend.aggregation.converter;

import java.util.Base64;
import se.tink.backend.aggregation.api.CallbackHostConfiguration;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;

public class CallbackHostConfigurationConverter {
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    public static CallbackHostConfiguration convert(ClusterInfo clusterInfo){
        CallbackHostConfiguration callbackHostConfiguration = new CallbackHostConfiguration();

        callbackHostConfiguration.setApiToken(clusterInfo.getApiToken());
        callbackHostConfiguration.setBase64encodedclientcert(
                BASE64_ENCODER.encodeToString(clusterInfo.getClientCertificate()));
        callbackHostConfiguration.setClusterId(clusterInfo.getClusterId().getId());
        callbackHostConfiguration.setDisablerequestcompression(clusterInfo.isDisableRequestCompression());
        callbackHostConfiguration.setHost(clusterInfo.getAggregationControllerHost());

        return callbackHostConfiguration;
    }
}
