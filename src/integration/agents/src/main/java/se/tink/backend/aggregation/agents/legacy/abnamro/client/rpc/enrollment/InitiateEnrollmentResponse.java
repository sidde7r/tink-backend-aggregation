package se.tink.backend.aggregation.agents.abnamro.client.rpc.enrollment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InitiateEnrollmentResponse {
    @JsonProperty("uuid")
    private String token;

    public String getToken() {
        return token;
    }
}
