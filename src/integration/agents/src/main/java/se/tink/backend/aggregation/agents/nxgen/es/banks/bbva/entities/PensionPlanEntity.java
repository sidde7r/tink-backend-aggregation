package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class PensionPlanEntity extends AbstractContractDetailsEntity {
    private AmountEntity liquidValue;

    private double shares;
    private AmountEntity balance;

    public AmountEntity getLiquidValue() {
        return liquidValue;
    }

    public double getShares() {
        return shares;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public InvestmentAccount toInvestmentAccount() {
        return InvestmentAccount.nxBuilder()
                .withoutPortfolios()
                .withCashBalance(getCashBalance())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getId())
                                .withAccountNumber(getId())
                                .withAccountName(getProduct().getDescription())
                                .addIdentifier(AccountIdentifier.create(Type.TINK, getId()))
                                .setProductName(getProduct().getName())
                                .build())
                .setApiIdentifier(getId())
                .build();
    }

    private ExactCurrencyAmount getCashBalance() {
        return balance.toTinkAmount();
    }
}
