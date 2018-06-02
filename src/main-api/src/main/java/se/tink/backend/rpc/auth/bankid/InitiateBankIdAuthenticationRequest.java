package se.tink.backend.rpc.auth.bankid;

import io.protostuff.Tag;

public class InitiateBankIdAuthenticationRequest {
    @Tag(1)
    private String nationalId;
    @Tag(2)
    private String market;

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }
}
