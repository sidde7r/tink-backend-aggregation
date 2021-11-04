package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LoanDetailsAccountEntity {
    private String type;
    private TermsAndConditionsEntity termsAndConditions;
    private InterestRateEntity interestRate;
    private AmountEntity amortizationOnDueDate;
    private String amortizationPlan;
    private String paymentIntervalAmortization;
    private String paymentIntervalInterest;
    private String fixedInterestPeriod;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date interestFixedToDate;

    private int remainingRepaymentTime;
    private List<CollateralsEntity> collaterals;
    private List<BorrowerEntity> borrowers;
    private boolean showFutureLoanPayments;
    private boolean showHistoricalPayments;
}
