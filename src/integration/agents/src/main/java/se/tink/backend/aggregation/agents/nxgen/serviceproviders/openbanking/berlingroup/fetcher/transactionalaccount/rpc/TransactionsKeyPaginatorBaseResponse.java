package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.TransactionsBaseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@Slf4j
@JsonObject
public class TransactionsKeyPaginatorBaseResponse
        implements TransactionKeyPaginatorResponse<String> {
    private TransactionsBaseEntity transactions;

    public TransactionsBaseEntity getTransactions() {
        return transactions;
    }

    public Collection<Transaction> toTinkTransactions() {
        return transactions.toTinkTransactions();
    }

    @Override
    public String nextKey() {
        return transactions.getNextLink();
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.toTinkTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        log.info(
                "Booked transactions amount on page is : {}",
                CollectionUtils.size(transactions.getBooked()));
        log.info(
                "Pending transaction amount on page is: {}",
                CollectionUtils.size(transactions.getPending()));
        return Optional.of(transactions.hasMore());
    }
}
