package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount;

import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Fetchers;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.LogTags;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.AccountTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;

public class BbvaTransactionFetcher implements TransactionPagePaginator<TransactionalAccount> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BbvaTransactionFetcher.class);

    private BbvaApiClient apiClient;

    public BbvaTransactionFetcher(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        return fetchWithBackoffAndRetry(account, page, 1);
    }

    private AccountTransactionsResponse fetchWithBackoffAndRetry(
            TransactionalAccount account, int page, int attempt) {
        final int keyIndex = page * Fetchers.PAGE_SIZE;

        return Try.of(() -> apiClient.fetchAccountTransactions(account, keyIndex))
                .recover(
                        HttpClientException.class,
                        e -> {
                            logRetry(account, keyIndex, attempt);
                            backoffAWhile();
                            return fetchWithBackoffAndRetry(account, page, attempt + 1);
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

    private void logRetry(TransactionalAccount account, int keyIndex, int attempt) {
        final String accountNumber = account.getAccountNumber();

        LOGGER.warn(
                "{}: Retrying attempt {} for account {} with keyIndex {}",
                LogTags.TRANSACTIONS_RETRYING,
                attempt,
                accountNumber,
                keyIndex);
    }
}
