package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank;

import lombok.extern.slf4j.Slf4j;
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
                return handle401Or403ResponseErrorStatus(account, e);
            }
            throw e;
        }
    }
}
