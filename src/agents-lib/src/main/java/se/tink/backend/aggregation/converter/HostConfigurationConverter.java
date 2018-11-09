package se.tink.backend.aggregation.converter;

import se.tink.backend.aggregation.aggregationcontroller.v1.core.HostConfiguration;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;

import java.util.Base64;

public class HostConfigurationConverter {
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    public static HostConfiguration convert(ClusterInfo clusterInfo){
        return HostConfiguration.create(
                clusterInfo.getClusterId().getId(),
                clusterInfo.getAggregationControllerHost(),
                clusterInfo.getApiToken(),
                BASE64_ENCODER.encodeToString(clusterInfo.getClientCertificate()),
                clusterInfo.isDisableRequestCompression()
                );
    }
}
