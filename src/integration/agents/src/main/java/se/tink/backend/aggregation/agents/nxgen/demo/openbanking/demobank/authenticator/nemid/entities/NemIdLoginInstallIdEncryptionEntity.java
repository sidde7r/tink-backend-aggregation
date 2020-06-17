package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdLoginInstallIdEncryptionEntity {

    private String installId;
    private String userId;
    private String pinCode;

    public NemIdLoginInstallIdEncryptionEntity() {}

    public NemIdLoginInstallIdEncryptionEntity(String userId, String pinCode, String installId) {
        this.userId = userId;
        this.pinCode = pinCode;
        this.installId = installId;
    }

    public String getUserId() {
        return userId;
    }

    public String getInstallId() {
        return installId;
    }

    public String getPinCode() {
        return pinCode;
    }
}
