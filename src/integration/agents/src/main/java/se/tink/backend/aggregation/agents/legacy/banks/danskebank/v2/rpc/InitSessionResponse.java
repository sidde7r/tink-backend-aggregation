package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InitSessionResponse extends AbstractResponse {
    @JsonProperty("Token")
    protected String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
