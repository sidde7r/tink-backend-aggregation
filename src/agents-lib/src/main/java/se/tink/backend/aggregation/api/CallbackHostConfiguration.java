package se.tink.backend.aggregation.api;

import com.google.common.base.Splitter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.metrics.MetricId;

public class CallbackHostConfiguration {
    private static final Logger log = LoggerFactory.getLogger(CallbackHostConfiguration.class);

    private String clusterId;
    private String host;
    private String apiToken;
    private String base64encodedclientcert;
    private boolean disablerequestcompression;

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

    public static CallbackHostConfiguration createForTesting(String clusterId) {
        CallbackHostConfiguration callbackHostConfiguration = new CallbackHostConfiguration();

        callbackHostConfiguration.setClusterId(clusterId);

        return callbackHostConfiguration;
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
