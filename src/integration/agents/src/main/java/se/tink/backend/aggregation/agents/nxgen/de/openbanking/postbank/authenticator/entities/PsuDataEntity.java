package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PsuDataEntity {

    private String encryptedPassword;

    public PsuDataEntity(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }
}
