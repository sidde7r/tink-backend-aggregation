package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetCreditCardInfoRequest {
    private String regNo;
    private String accountNo;

    public GetCreditCardInfoRequest setRegNo(String regNo) {
        this.regNo = regNo;
        return this;
    }

    public GetCreditCardInfoRequest setAccountNo(String accountNo) {
        this.accountNo = accountNo;
        return this;
    }
}
