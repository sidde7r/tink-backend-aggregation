package se.tink.backend.aggregation.agents.legacy.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InitiateRequest {
    private UserCredentialsRequestEntity request;

    public UserCredentialsRequestEntity getRequest() {
        return request;
    }

    public void setRequest(UserCredentialsRequestEntity request) {
        this.request = request;
    }
}
