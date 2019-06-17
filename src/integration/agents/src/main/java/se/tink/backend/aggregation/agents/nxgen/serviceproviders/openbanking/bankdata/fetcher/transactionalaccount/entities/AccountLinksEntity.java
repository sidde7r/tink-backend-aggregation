package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountLinksEntity {

    BalancesLinkEntity balances;
    TransactionsLinkEntity transactions;

    public BalancesLinkEntity getBalances() {
        return balances;
    }

    public TransactionsLinkEntity getTransactions() {
        return transactions;
    }
}
