package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class Sparebank1TransactionFetcher implements TransactionPaginator<TransactionalAccount> {
    private final Sparebank1ApiClient apiClient;
    private final SessionStorage sessionStorage;

    public Sparebank1TransactionFetcher(Sparebank1ApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<Transaction> fetchTransactionsFor(TransactionalAccount account) {
        HashMap transactionUrlsByAccount = SerializationUtils.deserializeFromString(
                sessionStorage.get(Sparebank1Constants.Keys.ACCOUNT_TRANSACTION_URLS_KEY), HashMap.class);

        URL url = new URL((String) transactionUrlsByAccount.get(account.getBankIdentifier()));

        return apiClient.get(url, TransactionsResponse.class)
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
