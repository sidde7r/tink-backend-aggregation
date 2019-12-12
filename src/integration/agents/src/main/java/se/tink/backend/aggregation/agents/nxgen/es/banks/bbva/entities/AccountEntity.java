package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity extends AbstractContractDetailsEntity {

    private AmountEntity currentBalance;

    private AmountEntity availableBalanceLocalCurrency;
    private AmountEntity availableBalance;
    private AmountEntity currentBalanceLocalCurrency;

    public AmountEntity getCurrentBalance() {
        return currentBalance;
    }

    public AmountEntity getAvailableBalanceLocalCurrency() {
        return availableBalanceLocalCurrency;
    }

    public AmountEntity getAvailableBalance() {
        return availableBalance;
    }

    public AmountEntity getCurrentBalanceLocalCurrency() {
        return currentBalanceLocalCurrency;
    }
}
