package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.fetcher;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.apiclient.ArkeaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
public class ArkeaTransactionFetcher<T extends Account>
        implements TransactionKeyPaginator<T, String> {

    private final ArkeaApiClient apiClient;

    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            T account, String nextPagePath) {
        try {
            return apiClient.getTransactions(account.getApiIdentifier(), nextPagePath);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_NO_CONTENT) {
                return TransactionKeyPaginatorResponseImpl.createEmpty();
            }
            throw e;
        }
    }
}
