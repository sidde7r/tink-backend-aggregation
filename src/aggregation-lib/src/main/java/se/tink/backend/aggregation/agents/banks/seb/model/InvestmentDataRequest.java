package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvestmentDataRequest {
    private RequestWrappingEntity request;

    public RequestWrappingEntity getRequest() {
        return request;
    }

    public void setRequest(RequestWrappingEntity request) {
        this.request = request;
    }
}
