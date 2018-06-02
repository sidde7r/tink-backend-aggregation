package se.tink.backend.rpc.auth.keys;

public class DeleteAuthenticationKeyCommand {
    String userId;
    String keyId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }
}
