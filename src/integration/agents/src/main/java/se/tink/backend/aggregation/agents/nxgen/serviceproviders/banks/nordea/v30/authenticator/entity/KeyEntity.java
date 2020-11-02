package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KeyEntity {
    private String content;
    private String id;

    public static KeyEntity create(String content, String id) {
        KeyEntity keyEntity = new KeyEntity();
        keyEntity.id = id;
        keyEntity.content = content;
        return keyEntity;
    }
}
