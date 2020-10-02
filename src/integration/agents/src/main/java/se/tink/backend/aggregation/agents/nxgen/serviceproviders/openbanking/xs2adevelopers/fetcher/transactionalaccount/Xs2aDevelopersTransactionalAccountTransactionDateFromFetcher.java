package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.KeyWithInitiDateFromFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class Xs2aDevelopersTransactionalAccountTransactionDateFromFetcher
        implements KeyWithInitiDateFromFetcher<TransactionalAccount, String> {

    private static final int DEFAULT_MAX_DAYS_BACK = 90;
    private final Xs2aDevelopersApiClient apiClient;
    private final Xs2aDevelopersAuthenticator authenticator;
    private final int daysBack;

    public Xs2aDevelopersTransactionalAccountTransactionDateFromFetcher(
            Xs2aDevelopersApiClient apiClient, Xs2aDevelopersAuthenticator authenticator) {
        this(apiClient, authenticator, DEFAULT_MAX_DAYS_BACK);
    }

    public Xs2aDevelopersTransactionalAccountTransactionDateFromFetcher(
            Xs2aDevelopersApiClient apiClient,
            Xs2aDevelopersAuthenticator authenticator,
            int daysBack) {
        this.apiClient = apiClient;
        this.authenticator = authenticator;
        this.daysBack = daysBack;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        return apiClient.getTransactions(key);
    }

    @Override
    public TransactionKeyPaginatorResponse<String> fetchTransactionsFor(
            TransactionalAccount account, LocalDate dateFrom) {

        return apiClient.getTransactions(account, dateFrom);
    }

    @Override
    public LocalDate minimalFromDate() {
        return LocalDate.now().minusDays(daysBack);
    }
}
