package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InterestSpecificationEntity {
    private AmountEntity interest;
    private AmountEntity amount;
    private String interestRate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String periodDateFrom;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String periodDateTo;

    public AmountEntity getInterest() {
        return interest;
    }

    public AmountEntity getAmount() {
        return amount;
    }

    public String getInterestRate() {
        return interestRate;
    }

    public String getPeriodDateFrom() {
        return periodDateFrom;
    }

    public String getPeriodDateTo() {
        return periodDateTo;
    }
}
