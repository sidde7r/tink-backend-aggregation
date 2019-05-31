package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.transactionalaccount;

import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {
    private static final Logger LOG = LoggerFactory.getLogger(NordeaTransactionFetcher.class);
    private static final long TRANSACTION_FETCHER_BACKOFF = 2500;
    private static final int MAX_RETRY_ATTEMPTS = 2;
    private static final int FETCH_TRANSACTIONS_LIMIT = 30;
    private final NordeaFIApiClient apiClient;
    private final SessionStorage sessionStorage;

    public NordeaTransactionFetcher(NordeaFIApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        return fetchTransactions(account, key, 1);
    }

    private TransactionKeyPaginatorResponse<String> fetchTransactions(
            TransactionalAccount account, String key, int attempt) {
        try {
            return apiClient.fetchTransactions(
                    FETCH_TRANSACTIONS_LIMIT, key, account.getApiIdentifier());
        } catch (HttpResponseException hre) {
            return fetchWithBackoffAndRetry(hre, account, key, attempt);
        }
    }

    private TransactionKeyPaginatorResponse<String> fetchWithBackoffAndRetry(
            HttpResponseException hre, TransactionalAccount account, String key, int attempt) {

        if (hre.getResponse().getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR
                || hre.getResponse().getStatus() == HttpStatus.SC_GATEWAY_TIMEOUT) {
            if (attempt <= MAX_RETRY_ATTEMPTS) {
                backoffAWhile();
                LOG.debug(
                        String.format(
                                "Retry [%d] fetch transactions account[%s] after backoff ",
                                attempt, account.getAccountNumber()));

                return fetchTransactions(account, key, ++attempt);
            }
        }

        throw hre;
    }

    private void backoffAWhile() {
        try {
            Thread.sleep(TRANSACTION_FETCHER_BACKOFF);
        } catch (InterruptedException e) {
            LOG.debug("Woke up early");
        }
    }
}
