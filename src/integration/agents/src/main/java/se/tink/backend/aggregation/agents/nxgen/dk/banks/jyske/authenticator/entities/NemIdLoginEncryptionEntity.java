package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Encryptable;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdLoginEncryptionEntity implements Encryptable {
    private final String userId;
    private final String pinCode;

    public NemIdLoginEncryptionEntity(String userId, String pinCode) {
        this.userId = userId;
        this.pinCode = pinCode;
    }

    public String getUserId() {
        return userId;
    }

    public String getPinCode() {
        return pinCode;
    }
}
