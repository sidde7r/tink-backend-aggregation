package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Fetchers.RETRY_ATTEMPTS;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Fetchers.TIMEOUT_RETRY_SLEEP_MILLISECONDS;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;

public class BbvaCreditCardTransactionFetcher
        implements TransactionKeyPaginator<CreditCardAccount, String> {
    private final BbvaApiClient apiClient;

    public BbvaCreditCardTransactionFetcher(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    @SneakyThrows
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            CreditCardAccount account, String key) {
        Retryer<Optional<CreditCardTransactionsResponse>> creditCardTransactionsRetryer =
                getFetchCreditCardTransactionsRetryer();
        Optional<CreditCardTransactionsResponse> optionalResponse =
                creditCardTransactionsRetryer.call(
                        () -> apiClient.fetchCreditCardTransactions(account, key));
        return optionalResponse.isPresent()
                ? optionalResponse.get()
                : TransactionKeyPaginatorResponseImpl.createEmpty();
    }

    private Retryer<Optional<CreditCardTransactionsResponse>>
            getFetchCreditCardTransactionsRetryer() {
        return RetryerBuilder.<Optional<CreditCardTransactionsResponse>>newBuilder()
                .retryIfException(HttpClientException.class::isInstance)
                .withWaitStrategy(
                        WaitStrategies.fixedWait(
                                TIMEOUT_RETRY_SLEEP_MILLISECONDS, TimeUnit.MILLISECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(RETRY_ATTEMPTS))
                .build();
    }
}
