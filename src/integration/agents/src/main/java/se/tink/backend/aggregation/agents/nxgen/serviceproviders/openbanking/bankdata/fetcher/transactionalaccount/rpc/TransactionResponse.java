package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionResponse {
    private TransactionsEntity transactions;

    public TransactionsEntity getTransactions() {
        return transactions;
    }

    public Collection<Transaction> getTinkTransactions() {
        return transactions.toTinkTransactions();
    }

    public String nextKey() {
        return Optional.ofNullable(transactions.getLinks())
                .map(links -> links.get("next"))
                .map(LinkEntity::getHref)
                .orElse(null);
    }
}
