package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonAlias;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;

@JsonObject
public enum LoanTypeEntity {
    @JsonAlias({"mortgage", "MORTGAGE"})
    MORTGAGE,
    @JsonAlias({"consumer_credit", "CONSUMER_CREDIT"})
    CONSUMER_CREDIT,
    @JsonAlias({"blanco_loan", "BLANCO_LOAN"})
    BLANCO_LOAN;

    public LoanDetails.Type toTinkLoanType() {
        switch (this) {
            case MORTGAGE:
                return LoanDetails.Type.MORTGAGE;
            case CONSUMER_CREDIT:
            case BLANCO_LOAN:
                return LoanDetails.Type.BLANCO;
            default:
                return LoanDetails.Type.OTHER;
        }
    }
}
