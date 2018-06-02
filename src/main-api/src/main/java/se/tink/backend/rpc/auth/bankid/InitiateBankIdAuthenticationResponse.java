package se.tink.backend.rpc.auth.bankid;

import io.protostuff.Tag;
import se.tink.backend.core.auth.bankid.BankIdAuthenticationStatus;

public class InitiateBankIdAuthenticationResponse {
    @Tag(1)
    private String autostartToken;
    @Tag(2)
    private String authenticationToken;
    @Tag(3)
    private BankIdAuthenticationStatus status;

    public String getAutostartToken() {
        return autostartToken;
    }

    public void setAutostartToken(String autostartToken) {
        this.autostartToken = autostartToken;
    }

    public String getAuthenticationToken() {
        return authenticationToken;
    }

    public void setAuthenticationToken(String authenticationToken) {
        this.authenticationToken = authenticationToken;
    }

    public BankIdAuthenticationStatus getStatus() {
        return status;
    }

    public void setStatus(BankIdAuthenticationStatus status) {
        this.status = status;
    }
}
