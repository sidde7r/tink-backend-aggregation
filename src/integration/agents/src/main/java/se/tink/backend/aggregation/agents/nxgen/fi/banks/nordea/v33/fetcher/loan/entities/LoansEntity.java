package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoansEntity {
    @JsonProperty("loan_id")
    private String loanId;

    @JsonProperty("loan_formatted_id")
    private String loanFormattedId;

    @JsonProperty("product_code")
    private String productCode;

    @JsonProperty private String currency;
    @JsonProperty private String group;

    @JsonProperty("repayment_status")
    private String repaymentStatus;

    @JsonProperty private String nickname;
    @JsonProperty private AmountEntity amount;
    @JsonProperty private CreditEntity credit;

    public String getLoanId() {
        return loanId;
    }
}
