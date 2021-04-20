package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.Fetchers;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.LogTags;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.rpc.CajamarCreditCardTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;

@Slf4j
public class CajamarCreditCardTransactionFetcher
        implements TransactionKeyPaginator<CreditCardAccount, String> {

    private final CajamarApiClient apiClient;

    public CajamarCreditCardTransactionFetcher(CajamarApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            CreditCardAccount account, String key) {
        return fetchWithBackoffAndRetry(account, key, 1);
    }

    private CajamarCreditCardTransactionsResponse fetchWithBackoffAndRetry(
            CreditCardAccount account, String key, int attempt) {
        return Try.of(() -> apiClient.fetchCreditCardTransactions(account, key))
                .recover(
                        HttpClientException.class,
                        e -> {
                            logRetry(account, key, attempt, e);
                            backoffAWhile();
                            return fetchWithBackoffAndRetry(account, key, attempt + 1);
                        })
                .filterTry(
                        CajamarCreditCardTransactionsResponse.shouldRetryFetching(attempt),
                        () -> new RuntimeException(ErrorMessages.MAX_TRY_ATTEMPTS))
                .get();
    }

    private void backoffAWhile() {
        try {
            Thread.sleep(Fetchers.BACKOFF);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("Woke up early", e);
        }
    }

    private void logRetry(
            CreditCardAccount account, String key, int attempt, HttpClientException e) {
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
