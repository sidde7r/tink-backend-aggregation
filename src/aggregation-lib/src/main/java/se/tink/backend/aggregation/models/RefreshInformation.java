package se.tink.backend.aggregation.models;

import java.util.Map;
import org.slf4j.MDC;
import se.tink.backend.aggregation.cluster.identification.Aggregator;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.rpc.RefreshInformationRequest;

public class RefreshInformation {

    private RefreshInformationRequest request;

    private String aggregationControllerHost;
    private String apiToken;
    private byte[] clientCertificate;
    private boolean disableRequestCompression;
    private Map<String, String> context;

    private String name;
    private String environment;
    private Aggregator aggregator;

    public RefreshInformation() {
    }

    public RefreshInformation(RefreshInformationRequest request, ClusterInfo clusterInfo) {
        this.request = request;
        this.aggregationControllerHost = clusterInfo.getAggregationControllerHost();
        this.apiToken = clusterInfo.getApiToken();
        this.clientCertificate = clusterInfo.getClientCertificate();
        this.disableRequestCompression = clusterInfo.isDisableRequestCompression();
        this.name = clusterInfo.getClusterId().getName();
        this.environment = clusterInfo.getClusterId().getEnvironment();
        this.aggregator = clusterInfo.getClusterId().getAggregator();
        this.context = MDC.getCopyOfContextMap();
    }

    public RefreshInformationRequest getRequest() {
        return request;
    }

    public void setRequest(RefreshInformationRequest request) {
        this.request = request;
    }

    public String getAggregationControllerHost() {
        return aggregationControllerHost;
    }

    public void setAggregationControllerHost(String aggregationControllerHost) {
        this.aggregationControllerHost = aggregationControllerHost;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public byte[] getClientCertificate() {
        return clientCertificate;
    }

    public void setClientCertificate(byte[] clientCertificate) {
        this.clientCertificate = clientCertificate;
    }

    public boolean isDisableRequestCompression() {
        return disableRequestCompression;
    }

    public void setDisableRequestCompression(boolean disableRequestCompression) {
        this.disableRequestCompression = disableRequestCompression;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public Aggregator getAggregator() {
        return aggregator;
    }

    public void setAggregator(Aggregator aggregator) {
        this.aggregator = aggregator;
    }

    public void setMDCContext(Map<String, String> context){
        this.context = context;
    }

    public Map<String, String> getMDCContext(){
        return context;
    }
}
