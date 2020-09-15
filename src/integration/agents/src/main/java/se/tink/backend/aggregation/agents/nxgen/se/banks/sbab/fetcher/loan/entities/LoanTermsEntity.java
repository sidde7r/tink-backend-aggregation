package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LoanTermsEntity {
    private BigDecimal amortizationAmount;
    private String changeOfConditionDate;
    private String fixedInterestPeriod;
    private BigDecimal interestRate;
    private Date interestResetDate;
    private BigDecimal paymentTerm;
    private String startDate;
}
