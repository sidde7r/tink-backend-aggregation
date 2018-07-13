package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceStatusResponse {
    @JsonProperty("ServiceStatus")
    private ServiceStatusEntity serviceStatus;

    public ServiceStatusEntity getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(ServiceStatusEntity serviceStatus) {
        this.serviceStatus = serviceStatus;
    }
}
