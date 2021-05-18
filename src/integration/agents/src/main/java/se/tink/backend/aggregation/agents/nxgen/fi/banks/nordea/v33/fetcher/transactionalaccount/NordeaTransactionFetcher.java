package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.transactionalaccount;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
@RequiredArgsConstructor
public class NordeaTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private static final long TRANSACTION_FETCHER_BACKOFF = 2500;
    private static final int MAX_RETRY_ATTEMPTS = 2;
    private static final int FETCH_TRANSACTIONS_LIMIT = 30;
    private final NordeaFIApiClient apiClient;

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
                log.debug(
                        "Retry [{}] fetch transactions account[{}] after backoff ",
                        attempt,
                        account.getAccountNumber(),
                        hre);

                return fetchTransactions(account, key, ++attempt);
            }
        }

        throw hre;
    }

    private void backoffAWhile() {
        try {
            Thread.sleep(TRANSACTION_FETCHER_BACKOFF);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("Woke up early", e);
        }
    }
}
