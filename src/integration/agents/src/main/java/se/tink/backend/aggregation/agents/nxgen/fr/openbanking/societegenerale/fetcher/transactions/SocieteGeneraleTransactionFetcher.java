package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactions;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class SocieteGeneraleTransactionFetcher<T extends Account>
        implements TransactionKeyPaginator<T, URL> {

    private final SocieteGeneraleApiClient apiClient;

    @Override
    public TransactionKeyPaginatorResponse<URL> getTransactionsFor(T account, URL nextPageUrl) {
        try {
            return apiClient.getTransactions(account.getApiIdentifier(), nextPageUrl);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_NO_CONTENT) {
                return TransactionKeyPaginatorResponseImpl.createEmpty();
            }
            throw e;
        }
    }
}
