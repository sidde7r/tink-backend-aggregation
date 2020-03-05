package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PublicKeyEntity {

    private String key;
    private String type;

    public PublicKeyEntity(String key, String type) {
        this.key = key;
        this.type = type;
    }
}
