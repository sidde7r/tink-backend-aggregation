package se.tink.backend.aggregation.queue.models;

import java.util.Map;
import org.slf4j.MDC;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.libraries.credentials.service.RefreshInformationRequest;

public class RefreshInformation {

    private RefreshInformationRequest request;

    private String clientName;
    private String aggregatorId;
    private String clusterId;

    private String name;
    private String environment;
    private Map<String, String> context;

    public RefreshInformation() {
    }

    public RefreshInformation(RefreshInformationRequest request, ClientInfo clientInfo) {
        this.request = request;

        this.context = MDC.getCopyOfContextMap();
        this.clientName = clientInfo.getClientName();
        this.aggregatorId = clientInfo.getAggregatorId();
        this.clusterId = clientInfo.getClusterId();
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

    public RefreshInformationRequest getRequest() {
        return request;
    }

    public void setRequest(RefreshInformationRequest request) {
        this.request = request;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getAggregatorId() {
        return aggregatorId;
    }

    public void setAggregatorId(String aggregatorId) {
        this.aggregatorId = aggregatorId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public void setMDCContext(Map<String, String> context){
        this.context = context;
    }

    public Map<String, String> getMDCContext(){
        return context;
    }
}
