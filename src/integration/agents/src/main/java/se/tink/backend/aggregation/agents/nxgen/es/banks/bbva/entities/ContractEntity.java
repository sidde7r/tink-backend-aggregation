package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import io.vavr.control.Option;
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
    private SecuritiesPortfolioEntity securitiesPortfolio;

    public Option<CreditCardEntity> getCreditCard() {
        return Option.of(creditCard);
    }

    public Option<PensionPlanEntity> getPensionPlan() {
        return Option.of(pensionPlan);
    }

    public Option<DebitCardEntity> getDebitCard() {
        return Option.of(debitCard);
    }

    public Option<InsuranceEntity> getInsurance() {
        return Option.of(insurance);
    }

    public Option<PrepaidCardEntity> getPrepaidCard() {
        return Option.of(prepaidCard);
    }

    public Option<LoanEntity> getLoan() {
        return Option.of(loan);
    }

    public Option<AccountEntity> getAccount() {
        return Option.of(account);
    }

    public Option<SecuritiesPortfolioEntity> getSecuritiesPortfolio() {
        return Option.of(securitiesPortfolio);
    }
}
