package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount;

import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Fetchers;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.LogTags;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.AccountTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;

public class BbvaTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BbvaTransactionFetcher.class);

    private BbvaApiClient apiClient;

    public BbvaTransactionFetcher(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        return fetchWithBackoffAndRetry(account, key, 1);
    }

    private AccountTransactionsResponse fetchWithBackoffAndRetry(
            TransactionalAccount account, String key, int attempt) {
        return Try.of(() -> apiClient.fetchAccountTransactions(account, key))
                .recover(
                        HttpClientException.class,
                        e -> {
                            logRetry(account, key, attempt);
                            backoffAWhile();
                            return fetchWithBackoffAndRetry(account, key, attempt + 1);
                        })
                .filterTry(
                        AccountTransactionsResponse.shouldRetryFetching(attempt),
                        () -> new RuntimeException(ErrorMessages.MAX_TRY_ATTEMPTS))
                .get();
    }

    private void backoffAWhile() {
        try {
            Thread.sleep(Fetchers.BACKOFF);
        } catch (InterruptedException e) {
            LOGGER.debug("Woke up early");
        }
    }

    private void logRetry(TransactionalAccount account, String key, int attempt) {
        final String accountNumber = account.getAccountNumber();

        LOGGER.warn(
                "{}: Retrying attempt {} for account {} with key {}",
                LogTags.TRANSACTIONS_RETRYING,
                attempt,
                accountNumber,
                key);
    }
}
