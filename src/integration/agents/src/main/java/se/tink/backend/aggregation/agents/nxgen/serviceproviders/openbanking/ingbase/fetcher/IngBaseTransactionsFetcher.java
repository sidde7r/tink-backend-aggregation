package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class IngBaseTransactionsFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private final IngBaseApiClient apiClient;

    public IngBaseTransactionsFetcher(IngBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        Date now = new Date();
        long diffInMillies = Math.abs(now.getTime() - toDate.getTime());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        if (diff > IngBaseConstants.Transaction.MAX_IN_DAYS) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        try {
            String link = account.getFromTemporaryStorage(StorageKeys.TRANSACTIONS_URL);
            List<FetchTransactionsResponse> responseList = new ArrayList<>();

            while (link != null) {
                FetchTransactionsResponse response =
                        apiClient.fetchTransactions(link, fromDate, toDate);
                link = response.getNextLink();
                responseList.add(response);
            }
            return new TransactionsResponse(responseList);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == IngBaseConstants.ErrorMessages.FORBIDDEN) {
                return PaginatorResponseImpl.createEmpty(false);
            } else {
                throw e;
            }
        }
    }
}
