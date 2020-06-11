package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDateFromFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class Xs2aDevelopersTransactionalAccountTransactionDateFromFetcher
        implements TransactionDateFromFetcher<TransactionalAccount> {

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
    public List<? extends AggregationTransaction> fetchTransactionsFor(
            TransactionalAccount account, LocalDate fromDate) {
        try {
            return apiClient.getTransactions(account, fromDate, LocalDate.now());
        } catch (HttpResponseException e) {
            if (isNoMoreTransactionsAvailableToFetchException(e)) {
                return Collections.emptyList();
            } else if (isConsentInvalid(e)) {
                authenticator.invalidateToken();
                throw BankServiceError.CONSENT_EXPIRED.exception(e.getMessage());
            }
            throw e;
        }
    }

    @Override
    public LocalDate minimalFromDate() {
        return LocalDate.now().minusDays(daysBack);
    }

    private boolean isNoMoreTransactionsAvailableToFetchException(HttpResponseException ex) {
        return ex.getResponse().getStatus()
                        == Xs2aDevelopersConstants.Transactions.ERROR_CODE_MAX_ACCESS_EXCEEDED
                || ex.getResponse().getStatus()
                        == Xs2aDevelopersConstants.Transactions.ERROR_CODE_SERVICE_UNAVAILABLE;
    }

    private boolean isConsentInvalid(HttpResponseException ex) {
        return ex.getResponse().getStatus()
                        == Xs2aDevelopersConstants.Transactions.ERROR_CODE_CONSENT_INVALID
                || isConsentTimeoutException(ex);
    }

    private boolean isConsentTimeoutException(HttpResponseException ex) {
        return ex.getResponse().getStatus() == 400
                && ex.getResponse().getBody(String.class).contains("CONSENT_TIME_OUT_EXPIRED");
    }
}
