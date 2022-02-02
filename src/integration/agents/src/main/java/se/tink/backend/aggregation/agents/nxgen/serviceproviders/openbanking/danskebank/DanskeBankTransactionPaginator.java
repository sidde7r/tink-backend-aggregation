package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import se.tink.agent.sdk.operation.Provider;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.TransactionConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.UkOpenBankingTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
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

    DanskeBankTransactionPaginator(
            AgentComponentProvider componentProvider,
            Provider provider,
            UkOpenBankingAisConfig ukOpenBankingAisConfig,
            PersistentStorage persistentStorage,
            UkOpenBankingApiClient apiClient,
            Class<T> responseType,
            TransactionConverter<T, S> transactionConverter,
            LocalDateTimeSource localDateTimeSource,
            TransactionPaginationHelper paginationHelper) {
        super(
                componentProvider,
                provider,
                ukOpenBankingAisConfig,
                persistentStorage,
                apiClient,
                responseType,
                transactionConverter,
                localDateTimeSource,
                paginationHelper);

        retryExecutor.setRetryPolicy(
                new RetryPolicy(RETRY_FAILED_TRANSACTION_MAX_ATTEMPTS, BankServiceException.class));
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
            if (shouldRecoverFetchingTransactions(e.getResponse().getStatus())) {
                return recoverFetchingTransactions(account, key, e);
            }
            throw e;
        }
    }
}
