package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.embeddedotp;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EmbeddedChallengeResponse {
    private String message;

    public String getMessage() {
        return message;
    }
}
