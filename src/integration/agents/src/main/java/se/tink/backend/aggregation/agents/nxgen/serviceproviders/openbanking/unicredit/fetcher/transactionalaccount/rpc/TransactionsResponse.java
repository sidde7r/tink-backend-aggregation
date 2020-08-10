package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.transaction.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.transaction.TransactionsLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.transaction.TransactionsWrapperEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@JsonObject
public class TransactionsResponse implements TransactionKeyPaginatorResponse<URL> {

    private TransactionsWrapperEntity transactions;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        List<Transaction> booked = getTinkTransactions(transactions.getBooked(), false);
        List<Transaction> pending = getTinkTransactions(transactions.getPending(), true);

        return ListUtils.union(booked, pending);
    }

    private List<Transaction> getTinkTransactions(
            List<TransactionEntity> transactionEntities, boolean isPending) {
        return transactionEntities.stream()
                .map(te -> te.toTinkTransaction(isPending))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        TransactionsLinksEntity links = transactions.getLinks();
        return Optional.of(links != null && links.hasNext());
    }

    @Override
    public URL nextKey() {
        TransactionsLinksEntity links = transactions.getLinks();
        return links != null && links.hasNext() ? new URL(links.getNext()) : null;
    }
}
