package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.transactionalaccount;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SpankkiTransactionFetcher implements TransactionPagePaginator<TransactionalAccount> {
    private static final Logger LOG = LoggerFactory.getLogger(SpankkiTransactionFetcher.class);
    private final SpankkiApiClient apiClient;
    // Bank has issues when trying to fetch many pages of transactions
    private static final int GOOD_ENOUGH_NUMBER_OF_PAGES = 150;
    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final long TRANSACTION_FETCHER_BACKOFF = 2500;

    public SpankkiTransactionFetcher(SpankkiApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        if (page <= GOOD_ENOUGH_NUMBER_OF_PAGES) {
            return fetchTransactions(account, String.valueOf(page), 1);
        }
        return PaginatorResponseImpl.createEmpty(false);
    }

    private PaginatorResponse fetchTransactions(
            TransactionalAccount account, String page, int attempt) {
        try {
            return apiClient.fetchTransactions(account.getApiIdentifier(), page);
        } catch (HttpResponseException hre) {
            apiClient.keepAlive(); // refresh token
            return fetchWithBackoffAndRetry(hre, account, page, attempt);
        }
    }

    // Bank sometimes returns 500 HTTP response sporadically when fetching transactions
    private PaginatorResponse fetchWithBackoffAndRetry(
            HttpResponseException hre, TransactionalAccount account, String page, int attempt) {
        if (hre.getResponse().getStatus() == org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR
                && attempt <= MAX_RETRY_ATTEMPTS) {
            backoffAWhile();
            LOG.debug(
                    String.format(
                            "Retry [%d] fetch transactions account[%s], page[%s], after backoff ",
                            attempt, account.getAccountNumber(), page));

            return fetchTransactions(account, page, ++attempt);
        }

        throw hre;
    }

    private void backoffAWhile() {
        try {
            Thread.sleep(TRANSACTION_FETCHER_BACKOFF);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.debug("Woke up early");
        }
    }
}
