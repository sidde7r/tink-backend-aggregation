package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
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

    public String getType() {
        return type;
    }

    public TermsAndConditionsEntity getTermsAndConditions() {
        return termsAndConditions;
    }

    public InterestRateEntity getInterestRate() {
        return interestRate;
    }

    public AmountEntity getAmortizationOnDueDate() {
        return amortizationOnDueDate;
    }

    public String getAmortizationPlan() {
        return amortizationPlan;
    }

    public String getPaymentIntervalAmortization() {
        return paymentIntervalAmortization;
    }

    public String getPaymentIntervalInterest() {
        return paymentIntervalInterest;
    }

    public String getFixedInterestPeriod() {
        return fixedInterestPeriod;
    }

    public Date getInterestFixedToDate() {
        return interestFixedToDate;
    }

    public int getRemainingRepaymentTime() {
        return remainingRepaymentTime;
    }

    public List<CollateralsEntity> getCollaterals() {
        return collaterals;
    }

    public List<BorrowerEntity> getBorrowers() {
        return borrowers;
    }

    public boolean isShowFutureLoanPayments() {
        return showFutureLoanPayments;
    }

    public boolean isShowHistoricalPayments() {
        return showHistoricalPayments;
    }
}
