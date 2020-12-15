package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank;

import java.lang.invoke.MethodHandles;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.TransactionConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.UkOpenBankingTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.Account;
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

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    DanskeBankTransactionPaginator(
            UkOpenBankingAisConfig ukOpenBankingAisConfig,
            PersistentStorage persistentStorage,
            UkOpenBankingApiClient apiClient,
            Class<T> responseType,
            TransactionConverter<T, S> transactionConverter,
            LocalDateTimeSource localDateTimeSource) {
        super(
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
        updateAccountPaginationCount(account.getApiIdentifier());
        if (isPaginationCountOverLimit()) {
            return TransactionKeyPaginatorResponseImpl.createEmpty();
        }
        key = initialisePaginationKeyIfNull(account, key);

        try {
            // Danske Bank sometimes throws an error when we try to fetch transactions too far in
            // time, although we are using links provided by them in response. It is Danske internal
            // bug.
            String finalKey = key;
            return retryExecutor.execute(
                    (RetryCallback<TransactionKeyPaginatorResponse<String>, BankServiceException>)
                            () -> fetchTransactions(account, finalKey));
        } catch (BankServiceException e) {
            log.warn("Ignoring http 500 (Internal server error) in pagination.", e);
            return TransactionKeyPaginatorResponseImpl.createEmpty();
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 401 || e.getResponse().getStatus() == 403) {
                return recover401Or403ResponseErrorStatus(account, e);
            }
            throw e;
        }
    }

    @Override
    protected String initialisePaginationKeyIfNull(S account, String key) {
        if (key == null) {
            final OffsetDateTime fromDate =
                    getLastTransactionsFetchedDate(account.getApiIdentifier());

            key =
                    ukOpenBankingAisConfig.getInitialTransactionsPaginationKey(
                                    account.getApiIdentifier())
                            + FROM_BOOKING_DATE_TIME
                            + DateTimeFormatter.ISO_INSTANT.format(fromDate);
        }
        return key;
    }

    @Override
    protected TransactionKeyPaginatorResponse<String> recover401Or403ResponseErrorStatus(
            S account, HttpResponseException e) {
        String key;
        logger.error(
                "Trying to fetch transactions again for last 89 days. Got 401 in previous request",
                e);

        key =
                ukOpenBankingAisConfig.getInitialTransactionsPaginationKey(
                                account.getApiIdentifier())
                        + FROM_BOOKING_DATE_TIME
                        + DateTimeFormatter.ISO_INSTANT.format(
                                localDateTimeSource.now().minusDays(DEFAULT_MAX_ALLOWED_DAYS));
        return fetchTransactions(account, key);
    }
}
