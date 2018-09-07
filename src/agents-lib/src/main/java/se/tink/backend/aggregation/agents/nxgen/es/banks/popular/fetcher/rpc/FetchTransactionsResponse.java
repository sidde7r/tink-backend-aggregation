package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities.BancoPopularResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.entities.TransactionsWrapperEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class FetchTransactionsResponse extends BancoPopularResponse {
@JsonObject
    private TransactionsWrapperEntity customBtd6ECOAS211F;

    public Collection<Transaction> getTinkTransactions() {
        if (customBtd6ECOAS211F != null && customBtd6ECOAS211F.getTransactionList() != null) {
            return customBtd6ECOAS211F.getTransactionList().stream()
                    .map(TransactionEntity::toTinkTransaction)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    }

    public TransactionsWrapperEntity getCustomBtd6ECOAS211F() {
        return customBtd6ECOAS211F;
    }
}
