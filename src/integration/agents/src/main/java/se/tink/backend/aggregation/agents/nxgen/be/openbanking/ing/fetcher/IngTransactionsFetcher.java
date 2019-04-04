package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class IngTransactionsFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private final IngApiClient apiClient;

    public IngTransactionsFetcher(IngApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, Date fromDate,
        Date toDate) {

        FetchTransactionsResponse response = apiClient
            .fetchTransactions(account.getFromTemporaryStorage(
                IngConstants.StorageKeys.TRANSACTIONS_URL), fromDate, toDate);

        response.setFetchNextConsumer(apiClient::fetchTransactions);

        return response;
    }
}
