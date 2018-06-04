package se.tink.backend.encryption.rpc;


public class EncryptionRequest {
    private String payload;
    private EncryptionKeySet keySet;

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public EncryptionKeySet getKeySet() {
        return this.keySet;
    }

    public void setKeySet(EncryptionKeySet keySet) {
        this.keySet = keySet;
    }
}
