package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UpdateTransactionsAccountEntity {

    private CurrentBalanceEntity currentBalance;

    public CurrentBalanceEntity getCurrentBalance() {
        return currentBalance;
    }
}
