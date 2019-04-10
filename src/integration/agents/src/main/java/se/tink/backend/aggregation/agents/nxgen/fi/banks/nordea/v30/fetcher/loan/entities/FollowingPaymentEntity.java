package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FollowingPaymentEntity {

    private String date;
    private double instalment;
    private double interest;
    private int fees;

    @JsonProperty("account_mgmt_fee")
    private int accountMgmtFee;

    private int total;

    double getInstalmentValue() {
        return instalment;
    }
}
