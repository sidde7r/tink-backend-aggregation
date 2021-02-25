package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.account;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.Fetchers;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.LogTags;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.account.rpc.CajamarAccountTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;

@Slf4j
public class CajamarAccountTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private final CajamarApiClient apiClient;

    public CajamarAccountTransactionFetcher(CajamarApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        return fetchWithBackoffAndRetry(account, key, 1);
    }

    private CajamarAccountTransactionsResponse fetchWithBackoffAndRetry(
            TransactionalAccount account, String key, int attempt) {
        return Try.of(() -> apiClient.fetchAccountTransactions(account, key))
                .recover(
                        HttpClientException.class,
                        e -> {
                            logRetry(account, key, attempt, e);
                            backoffAWhile();
                            return fetchWithBackoffAndRetry(account, key, attempt + 1);
                        })
                .filterTry(
                        CajamarAccountTransactionsResponse.shouldRetryFetching(attempt),
                        () -> new RuntimeException(ErrorMessages.MAX_TRY_ATTEMPTS))
                .get();
    }

    private void backoffAWhile() {
        try {
            Thread.sleep(Fetchers.BACKOFF);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("Woke up too early", e);
        }
    }

    private void logRetry(
            TransactionalAccount account, String key, int attempt, HttpClientException e) {
        final String accountNumber = account.getAccountNumber();

        log.warn(
                "{}: Retrying attempt {} for account {} with key {}",
                LogTags.TRANSACTIONS_RETRYING,
                attempt,
                accountNumber,
                key,
                e);
    }
}
