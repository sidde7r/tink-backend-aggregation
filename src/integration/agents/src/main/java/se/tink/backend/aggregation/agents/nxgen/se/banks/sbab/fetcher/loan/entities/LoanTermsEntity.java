package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities;

import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanTermsEntity {
    private BigDecimal amortizationAmount;
    private Date changeOfConditionDate;
    private String fixedIncomePeriod;
    private BigDecimal interestRate;
    private Date interestResetDate;
    private BigDecimal paymentTerm;
    private Date startDate;

    public BigDecimal getAmortizationAmount() {
        return amortizationAmount;
    }

    public Date getChangeOfConditionDate() {
        return changeOfConditionDate;
    }

    public String getFixedIncomePeriod() {
        return fixedIncomePeriod;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public Date getInterestResetDate() {
        return interestResetDate;
    }

    public BigDecimal getPaymentTerm() {
        return paymentTerm;
    }

    public Date getStartDate() {
        return startDate;
    }
}
