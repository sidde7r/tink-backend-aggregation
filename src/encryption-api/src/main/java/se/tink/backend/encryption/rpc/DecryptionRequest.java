package se.tink.backend.encryption.rpc;


public class DecryptionRequest {
    private EncryptionKeySet keySet;
    private String payload;

    public String getPayload() {
        return this.payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public EncryptionKeySet getKeySet() {
        return keySet;
    }

    public void setKeySet(EncryptionKeySet keySet) {
        this.keySet = keySet;
    }
}
