package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanDetailsAccountEntity {
    private LoanDetailsTypeEnum type;
    private InterestRateEntity interestRate;
    private AmountEntity amortizationOnDueDate;
    private String amortizationPlan;
    private String paymentIntervalAmortization;
    private String paymentIntervalInterest;
    private List<CollateralsEntity> collaterals;
    private List<BorrowerEntity> borrowers;

    public List<CollateralsEntity> getCollaterals() {
        return collaterals;
    }

    public List<BorrowerEntity> getBorrowers() {
        return borrowers;
    }
}
