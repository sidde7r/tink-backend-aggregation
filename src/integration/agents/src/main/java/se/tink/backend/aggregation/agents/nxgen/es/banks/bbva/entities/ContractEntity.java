package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContractEntity {
    private CreditCardEntity creditCard;
    private PensionPlanEntity pensionPlan;
    private DebitCardEntity debitCard;
    private InsuranceEntity insurance;
    private PrepaidCardEntity prepaidCard;
    private LoanEntity loan;
    private AccountEntity account;

    public Optional<CreditCardEntity> getCreditCard() {
        return Optional.ofNullable(creditCard);
    }

    public Optional<PensionPlanEntity> getPensionPlan() {
        return Optional.ofNullable(pensionPlan);
    }

    public Optional<DebitCardEntity> getDebitCard() {
        return Optional.ofNullable(debitCard);
    }

    public Optional<InsuranceEntity> getInsurance() {
        return Optional.ofNullable(insurance);
    }

    public Optional<PrepaidCardEntity> getPrepaidCard() {
        return Optional.ofNullable(prepaidCard);
    }

    public Optional<LoanEntity> getLoan() {
        return Optional.ofNullable(loan);
    }

    public Optional<AccountEntity> getAccount() {
        return Optional.ofNullable(account);
    }
}
