package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Encryptable;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdLoginInstallIdEncryptionEntity implements Encryptable {

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
