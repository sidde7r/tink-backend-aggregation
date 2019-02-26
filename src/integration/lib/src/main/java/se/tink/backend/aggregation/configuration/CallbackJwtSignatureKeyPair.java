package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CallbackJwtSignatureKeyPair {
    @JsonProperty
    private String privateKeyPath;
    @JsonProperty
    private String publicKeyPath;


    @JsonProperty
    private boolean isEnabled = false;

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public String getPublicKeyPath() {
        return publicKeyPath;
    }

    public void setPublicKeyPath(String publicKeyPath) {
        this.publicKeyPath = publicKeyPath;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
