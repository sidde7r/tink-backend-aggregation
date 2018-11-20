package se.tink.backend.aggregation.aggregationcontroller.v1.core;

import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.metrics.MetricId;

import java.util.Base64;
import java.util.List;

public class HostConfiguration {
    private String clusterId;
    private String host;
    private String apiToken;
    private String base64encodedclientcert;
    private boolean disablerequestcompression;


    private static final Logger log = LoggerFactory.getLogger(HostConfiguration.class);
    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getBase64encodedclientcert() {
        return base64encodedclientcert;
    }

    public void setBase64encodedclientcert(String base64encodedclientcert) {
        this.base64encodedclientcert = base64encodedclientcert;
    }

    public boolean isDisablerequestcompression() {
        return disablerequestcompression;
    }

    public void setDisablerequestcompression(boolean disablerequestcompression) {
        this.disablerequestcompression = disablerequestcompression;
    }

    public byte[] getClientCert() {
        return BASE64_DECODER.decode(base64encodedclientcert);
    }


    public static HostConfiguration createForTesting(String clusterId) {
        HostConfiguration hostConfiguration = new HostConfiguration();

        hostConfiguration.setClusterId(clusterId);

        return hostConfiguration;
    }

    // TODO: We should do this some other way. This is a hack we can use for now.
    public MetricId.MetricLabels metricLabels() {
        List<String> splitClusterId = Splitter.on("-").splitToList(clusterId);

        if (splitClusterId.size() != 2) {
            // The clusterId should be of the format <cluster name>-<environment>, i.e. oxford-staging
            log.warn("SplitClusterId did not have size of exactly 2. ClusterId: {}", clusterId);
            return MetricId.MetricLabels.createEmpty();
        }

        return new MetricId.MetricLabels()
                .add("request_cluster", splitClusterId.get(0))
                .add("request_environment", splitClusterId.get(1));
    }
}
