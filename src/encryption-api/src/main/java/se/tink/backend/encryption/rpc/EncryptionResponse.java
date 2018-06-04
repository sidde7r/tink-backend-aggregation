package se.tink.backend.encryption.rpc;

import java.util.List;

public class EncryptionResponse {
    private List<String> keys;
    private String payload;

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> pieces) {
        this.keys = pieces;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
