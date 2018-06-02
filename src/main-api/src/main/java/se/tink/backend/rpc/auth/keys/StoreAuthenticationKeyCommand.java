package se.tink.backend.rpc.auth.keys;

import se.tink.backend.core.auth.AuthenticationSource;
import se.tink.backend.core.auth.UserPublicKeyType;

public class StoreAuthenticationKeyCommand {
    private String authenticationToken;
    private String key;
    private AuthenticationSource source;
    private String deviceId;
    private UserPublicKeyType type;

    public String getAuthenticationToken() {
        return authenticationToken;
    }

    public void setAuthenticationToken(String authenticationToken) {
        this.authenticationToken = authenticationToken;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public AuthenticationSource getSource() {
        return source;
    }

    public void setSource(AuthenticationSource source) {
        this.source = source;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public UserPublicKeyType getType() {
        return type;
    }

    public void setType(UserPublicKeyType type) {
        this.type = type;
    }
}
