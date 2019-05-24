package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount.entity.transaction.TransactionAccountInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount.entity.transaction.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount.entity.transaction.Transactions;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse implements TransactionKeyPaginatorResponse<String> {

    private TransactionAccountInfoEntity account;
    private Transactions transactions;

    @Override
    public String nextKey() {
        return null;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Stream.concat(
                        transactions.getBooked().stream()
                                .map(TransactionEntity::toBookedTinkTransaction),
                        transactions.getPending().stream()
                                .map(TransactionEntity::toPendingTinkTransaction))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(false);
    }
}
