package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AppSyncAndroidRequest {

    private String cipher;
    private String encryptedPayload;
    private String key;
    private String label;
    private PayloadAndroidEntity payload;

    public String getCipher() {
        return cipher;
    }

    public void setCipher(String cipher) {
        this.cipher = cipher;
    }

    public String getEncryptedPayload() {
        return encryptedPayload;
    }

    public void setEncryptedPayload(String encryptedPayload) {
        this.encryptedPayload = encryptedPayload;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public PayloadAndroidEntity getPayload() {
        return payload;
    }

    public void setPayload(
            PayloadAndroidEntity payload) {
        this.payload = payload;
    }
}
