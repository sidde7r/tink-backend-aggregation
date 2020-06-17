package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdLoginEncryptionEntity {
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
