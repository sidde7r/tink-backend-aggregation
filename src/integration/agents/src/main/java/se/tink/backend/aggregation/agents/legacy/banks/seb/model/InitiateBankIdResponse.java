package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InitiateBankIdResponse {
    private String status;

    @JsonProperty("next_request")
    private NextRequestEntity nextRequest;

    @JsonProperty("autostarttoken")
    private String autostartToken;

    private String message;
    private String rfa;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public NextRequestEntity getNextRequest() {
        return nextRequest;
    }

    public void setNextRequest(NextRequestEntity nextRequest) {
        this.nextRequest = nextRequest;
    }

    public String getAutostartToken() {
        return autostartToken;
    }

    public void setAutostartToken(String autostartToken) {
        this.autostartToken = autostartToken;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRfa() {
        return rfa;
    }

    public void setRfa(String rfa) {
        this.rfa = rfa;
    }
}
