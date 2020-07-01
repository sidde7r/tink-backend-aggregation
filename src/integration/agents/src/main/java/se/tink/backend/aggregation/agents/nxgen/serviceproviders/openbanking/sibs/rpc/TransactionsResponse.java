package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.rpc;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.account.BookedEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.transaction.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse implements TransactionKeyPaginatorResponse<String> {

    private TransactionsEntity transactions;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.getBooked().stream()
                .map(BookedEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {

        return Optional.of(transactions.getLinks().canFetchMore());
    }

    @Override
    public String nextKey() {
        return transactions.getLinks().getNextKey();
    }
}
