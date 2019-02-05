package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetDepositsContentListRequest {
    private String regNo;
    private String depositNo;

    public GetDepositsContentListRequest setRegNo(String regNo) {
        this.regNo = regNo;
        return this;
    }

    public GetDepositsContentListRequest setDepositNo(String depositNo) {
        this.depositNo = depositNo;
        return this;
    }
}
