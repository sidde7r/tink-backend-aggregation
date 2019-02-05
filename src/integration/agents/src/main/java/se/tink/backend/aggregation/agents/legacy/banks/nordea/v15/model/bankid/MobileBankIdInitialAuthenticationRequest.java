package se.tink.backend.aggregation.agents.banks.nordea.v15.model.bankid;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MobileBankIdInitialAuthenticationRequest {
    @JsonProperty("initBankIdAuthenticationIn")
    private MobileBankIdInitialAuthenticationRequestData data;

    public MobileBankIdInitialAuthenticationRequestData getData() {
        return data;
    }

    public void setData(MobileBankIdInitialAuthenticationRequestData data) {
        this.data = data;
    }
}
