package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CurrentBalanceEntity {

    private AmountEntity accountingBalance;
    private AmountEntity availableBalance;

    public AmountEntity getAccountingBalance() {
        return accountingBalance;
    }

    public void setAccountingBalance(AmountEntity accountingBalance) {
        this.accountingBalance = accountingBalance;
    }

    public AmountEntity getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(AmountEntity availableBalance) {
        this.availableBalance = availableBalance;
    }
}
