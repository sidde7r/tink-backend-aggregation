package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SimpleAccountEntity {

    private CurrentBalanceEntity currentBalance;

    public CurrentBalanceEntity getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(CurrentBalanceEntity currentBalance) {
        this.currentBalance = currentBalance;
    }
}
