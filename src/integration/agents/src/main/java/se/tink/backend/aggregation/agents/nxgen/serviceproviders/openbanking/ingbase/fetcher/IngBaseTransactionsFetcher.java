package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher;

import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

public class IngBaseTransactionsFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private final IngBaseApiClient apiClient;
    private final TemporaryStorage temporaryStorage;

    public IngBaseTransactionsFetcher(IngBaseApiClient apiClient) {
        this.apiClient = apiClient;
        this.temporaryStorage = new TemporaryStorage();
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        String account_number = account.getAccountNumber();
        String link =
                Optional.ofNullable(temporaryStorage.get(account_number))
                        .orElse(account.getFromTemporaryStorage(StorageKeys.TRANSACTIONS_URL));

        FetchTransactionsResponse response = apiClient.fetchTransactions(link);
        String nextLink = response.getNextLink();

        if (nextLink != null) {
            temporaryStorage.put(account_number, nextLink);
            return response;
        } else {
            temporaryStorage.remove(account_number);
            return PaginatorResponseImpl.createEmpty(false);
        }
    }
}
