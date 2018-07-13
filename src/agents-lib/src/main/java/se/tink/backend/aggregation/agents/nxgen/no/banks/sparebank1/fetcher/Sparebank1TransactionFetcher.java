package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher;

import com.google.common.base.Strings;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class Sparebank1TransactionFetcher implements TransactionPaginator<TransactionalAccount> {
    private final Sparebank1ApiClient apiClient;

    public Sparebank1TransactionFetcher(Sparebank1ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<Transaction> fetchTransactionsFor(TransactionalAccount account) {
        LinkEntity transactionsLink = account.getTemporaryStorage(
                Sparebank1Constants.Keys.TRANSACTIONS_LINK, LinkEntity.class);

        if (transactionsLink == null || Strings.isNullOrEmpty(transactionsLink.getHref())) {
            return Collections.emptyList();
        }

        return apiClient.fetchTransactions(transactionsLink.getHref())
                .getTransactions()
                .stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public boolean canFetchMoreFor(TransactionalAccount account) {
        return false;
    }
}
