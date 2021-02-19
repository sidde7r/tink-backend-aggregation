package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class BbvaCreditCardTransactionFetcher
        implements TransactionKeyPaginator<CreditCardAccount, String> {
    private final BbvaApiClient apiClient;

    public BbvaCreditCardTransactionFetcher(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            CreditCardAccount account, String key) {

        try {
            return apiClient.fetchCreditCardTransactions(account, key);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_NO_CONTENT) {
                return TransactionKeyPaginatorResponseImpl.createEmpty();
            }
            throw e;
        }
    }
}
