package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.loan.entities;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BasicLoanInfo {
    private Double interestRateTotal;
    private String interestType;
    private Date nextInterestAdjustmentDate;
    private String loanNumber;
    private AmountEntity amount;

    public Double getInterestRateTotal() {
        return interestRateTotal;
    }

    public String getInterestType() {
        return interestType;
    }

    public Date getNextInterestAdjustmentDate() {
        return nextInterestAdjustmentDate;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    public AmountEntity getAmount() {
        return amount;
    }
}
