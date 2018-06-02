package se.tink.backend.rpc.auth;

import io.protostuff.Tag;
import se.tink.backend.core.auth.AuthenticationStatus;

public class AuthenticationResponse {
    @Tag(1)
    private String authenticationToken;
    @Tag(2)
    private AuthenticationStatus status;

    public String getAuthenticationToken() {
        return authenticationToken;
    }

    public void setAuthenticationToken(String authenticationToken) {
        this.authenticationToken = authenticationToken;
    }

    public AuthenticationStatus getStatus() {
        return status;
    }

    public void setStatus(AuthenticationStatus status) {
        this.status = status;
    }
}
