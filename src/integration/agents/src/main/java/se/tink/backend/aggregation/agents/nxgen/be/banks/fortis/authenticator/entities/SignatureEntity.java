package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignatureEntity {
    private String token;
    private String response;

    public SignatureEntity(String token, String response) {
        this.token = token;
        this.response = response;
    }
}
