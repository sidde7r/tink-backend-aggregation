package se.tink.backend.rpc.auth.keys;

import se.tink.backend.core.UserPublicKey;
import se.tink.backend.core.auth.AuthenticationSource;
import se.tink.backend.core.auth.UserPublicKeyType;

public class AuthenticationKeyResponse {
    private String id;
    private String key;
    private AuthenticationSource source;
    private UserPublicKeyType type;

    public AuthenticationKeyResponse(UserPublicKey userPublicKey) {
        this.id = userPublicKey.getId();
        this.key = userPublicKey.getPublicKey();
        this.source = userPublicKey.getAuthenticationSource();
        this.type = userPublicKey.getType();
    }

    public String getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public AuthenticationSource getSource() {
        return source;
    }

    public UserPublicKeyType getType() {
        return type;
    }
}
