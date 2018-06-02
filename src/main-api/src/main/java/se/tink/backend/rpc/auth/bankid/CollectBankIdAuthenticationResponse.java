package se.tink.backend.rpc.auth.bankid;

import io.protostuff.Tag;
import se.tink.backend.core.auth.bankid.BankIdAuthenticationStatus;

public class CollectBankIdAuthenticationResponse {
    @Tag(1)
    private BankIdAuthenticationStatus status;
    @Tag(2)
    private String nationalId;

    public BankIdAuthenticationStatus getStatus() {
        return status;
    }

    public void setStatus(BankIdAuthenticationStatus status) {
        this.status = status;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }
}
