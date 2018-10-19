package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.transactionalaccount.entities.HandelsbankenFITransaction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.URL;

public class TransactionsFIResponse extends TransactionsResponse implements
        TransactionKeyPaginatorResponse<URL> {

    private List<HandelsbankenFITransaction> transactions;

    @Override
    public List<Transaction> toTinkTransactions() {
        return transactions.stream()
                .map(HandelsbankenFITransaction::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public URL nextKey() {
        return searchLink(HandelsbankenConstants.URLS.Links.CARD_MORE_TRANSACTIONS).orElse(null);
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return toTinkTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(nextKey() != null);
    }
}
