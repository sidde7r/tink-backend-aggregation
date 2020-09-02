package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LoanTermsEntity {
    private BigDecimal amortizationAmount;
    private Date changeOfConditionDate;
    private String fixedIncomePeriod;
    private BigDecimal interestRate;
    private Date interestResetDate;
    private BigDecimal paymentTerm;
    private Date startDate;
}
