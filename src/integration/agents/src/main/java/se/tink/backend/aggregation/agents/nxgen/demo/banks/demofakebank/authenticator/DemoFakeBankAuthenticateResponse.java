package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.authenticator;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DemoFakeBankAuthenticateResponse {
    @JsonProperty("token")
    private String token;

    private String status;

    public String getToken() {
        return token;
    }

    public String getStatus() {
        return status;
    }
}
