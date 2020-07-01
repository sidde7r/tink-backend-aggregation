package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.embeddedotp;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EmbeddedCompleteResponse {
    private String token;

    public String getToken() {
        return token;
    }
}
