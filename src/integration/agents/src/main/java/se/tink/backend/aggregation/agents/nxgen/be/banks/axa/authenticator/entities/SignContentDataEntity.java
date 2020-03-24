package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignContentDataEntity {

    private String payload;
    private String signedPayload;

    public SignContentDataEntity(String payload, String signedPayload) {
        this.payload = payload;
        this.signedPayload = signedPayload;
    }
}
