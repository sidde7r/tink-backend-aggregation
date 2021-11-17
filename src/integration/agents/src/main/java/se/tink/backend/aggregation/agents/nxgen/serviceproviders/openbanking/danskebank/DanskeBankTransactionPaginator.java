package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants.Time;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.TransactionConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.UkOpenBankingTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.retrypolicy.RetryCallback;
import se.tink.libraries.retrypolicy.RetryExecutor;
import se.tink.libraries.retrypolicy.RetryPolicy;

@Slf4j
public class DanskeBankTransactionPaginator<T, S extends Account>
        extends UkOpenBankingTransactionPaginator<T, S> {

    private static final int RETRY_FAILED_TRANSACTION_MAX_ATTEMPTS = 3;
    private final RetryExecutor retryExecutor = new RetryExecutor();
    private final UkOpenBankingAisConfig ukOpenBankingAisConfig;

    DanskeBankTransactionPaginator(
            AgentComponentProvider componentProvider,
            Provider provider,
            UkOpenBankingAisConfig ukOpenBankingAisConfig,
            PersistentStorage persistentStorage,
            UkOpenBankingApiClient apiClient,
            Class<T> responseType,
            TransactionConverter<T, S> transactionConverter,
            LocalDateTimeSource localDateTimeSource) {
        super(
                componentProvider,
                provider,
                ukOpenBankingAisConfig,
                persistentStorage,
                apiClient,
                responseType,
                transactionConverter,
                localDateTimeSource);

        retryExecutor.setRetryPolicy(
                new RetryPolicy(RETRY_FAILED_TRANSACTION_MAX_ATTEMPTS, BankServiceException.class));
        this.ukOpenBankingAisConfig = ukOpenBankingAisConfig;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(S account, String key) {
        key = initialisePaginationKeyIfNull(account, key);
        updateAccountPaginationCount(account.getApiIdentifier());

        List<Transaction> transactions = new ArrayList<>();
        while (key != null && !isPaginationCountOverLimit()) {
            TransactionKeyPaginatorResponse<String> transactionsPage =
                    fetchTransactionsPage(account, key);
            transactions.addAll(transactionsPage.getTinkTransactions());
            key = transactionsPage.nextKey();
            updateAccountPaginationCount(account.getApiIdentifier());
        }
        return new TransactionKeyPaginatorResponseImpl<>(transactions, null);
    }

    private TransactionKeyPaginatorResponse<String> fetchTransactionsPage(S account, String key) {
        try {
            // Danske Bank sometimes throws an error when we try to fetch transactions too far in
            // time, although we are using links provided by them in response. It is Danske internal
            // bug.
            return retryExecutor.execute(
                    (RetryCallback<TransactionKeyPaginatorResponse<String>, BankServiceException>)
                            () -> fetchTransactions(account, key));
        } catch (BankServiceException e) {
            log.warn("Ignoring http 500 (Internal server error) in pagination.", e);
            return TransactionKeyPaginatorResponseImpl.createEmpty();
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 401 || e.getResponse().getStatus() == 403) {
                return recover401Or403ResponseErrorStatus(account, key, e);
            }
            throw e;
        }
    }

    @Override
    protected String initialisePaginationKeyIfNull(S account, String key) {
        if (key == null) {
            final OffsetDateTime fromDate = calculateFromBookingDate(account.getApiIdentifier());

            key =
                    ukOpenBankingAisConfig.getInitialTransactionsPaginationKey(
                                    account.getApiIdentifier())
                            + FROM_BOOKING_DATE_TIME
                            + ISO_OFFSET_DATE_TIME.format(fromDate);
        }
        return key;
    }

    @Override
    protected TransactionKeyPaginatorResponse<String> recover401Or403ResponseErrorStatus(
            S account, String key, HttpResponseException e) {

        if (isFirstPage()) {
            key =
                    ukOpenBankingAisConfig.getInitialTransactionsPaginationKey(
                                    account.getApiIdentifier())
                            + FROM_BOOKING_DATE_TIME
                            + ISO_OFFSET_DATE_TIME.format(
                                    localDateTimeSource
                                            .now(Time.DEFAULT_ZONE_ID)
                                            .minusDays(DEFAULT_MAX_ALLOWED_DAYS));
        }
        log.warn(
                "Retry fetching transactions for key {}. Got {} in previous request with the below exception\n{}",
                key,
                e.getResponse().getStatus(),
                ExceptionUtils.getStackTrace(e));

        return fetchTransactions(account, key);
    }
}
