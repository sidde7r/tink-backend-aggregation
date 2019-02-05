package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class Sparebank1TransactionFetcher implements TransactionPaginator<TransactionalAccount> {
    private final Sparebank1ApiClient apiClient;

    public Sparebank1TransactionFetcher(Sparebank1ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void resetState() {

    }

    @Override
    public PaginatorResponse fetchTransactionsFor(TransactionalAccount account) {
        Optional<LinkEntity> storedTransactionsLink = account.getFromTemporaryStorage(
                Sparebank1Constants.Keys.TRANSACTIONS_LINK, LinkEntity.class);

        if (!storedTransactionsLink.isPresent()) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        LinkEntity transactionsLink = storedTransactionsLink.get();
        if (Strings.isNullOrEmpty(transactionsLink.getHref())) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        List<Transaction> transactions = apiClient.fetchTransactions(transactionsLink.getHref())
                .getTransactions()
                .stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());

        return PaginatorResponseImpl.create(transactions, false);
    }
}
