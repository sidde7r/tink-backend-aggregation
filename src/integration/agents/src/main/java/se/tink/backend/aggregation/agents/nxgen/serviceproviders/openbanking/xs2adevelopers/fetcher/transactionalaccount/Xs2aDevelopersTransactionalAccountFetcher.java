package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.Transactions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class Xs2aDevelopersTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final Xs2aDevelopersApiClient apiClient;
    private final Xs2aDevelopersAuthenticator authenticator;

    public Xs2aDevelopersTransactionalAccountFetcher(
            Xs2aDevelopersApiClient apiClient, Xs2aDevelopersAuthenticator authenticator) {
        this.apiClient = apiClient;
        this.authenticator = authenticator;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        GetAccountsResponse getAccountsResponse = apiClient.getAccounts();
        return getAccountsResponse.getAccountList().stream()
                .map(this::transformAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> transformAccount(AccountEntity accountEntity) {
        accountEntity.setBalance(apiClient.getBalance(accountEntity).getBalances());
        return accountEntity.toTinkAccount();
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        try {
            return PaginatorResponseImpl.create(
                    apiClient.getTransactions(
                            account,
                            new java.sql.Date(fromDate.getTime()).toLocalDate(),
                            new java.sql.Date(toDate.getTime()).toLocalDate()));
        } catch (HttpResponseException e) {
            if (isNoMoreTransactionsAvailableToFetchException(e)) {
                return PaginatorResponseImpl.createEmpty(false);
            } else if (isConsentTimeoutException(e)) {
                authenticator.invalidateToken();
                throw BankServiceError.CONSENT_EXPIRED.exception(e.getMessage());
            }
            throw e;
        }
    }

    private boolean isNoMoreTransactionsAvailableToFetchException(HttpResponseException ex) {
        return ex.getResponse().getStatus() == Transactions.ERROR_CODE_MAX_ACCESS_EXCEEDED
                || ex.getResponse().getStatus() == Transactions.ERROR_CODE_SERVICE_UNAVAILABLE
                || ex.getResponse().getStatus() == Transactions.ERROR_CODE_CONSENT_INVALID;
    }

    private boolean isConsentTimeoutException(HttpResponseException ex) {
        return ex.getResponse().getStatus() == 400
                && ex.getResponse().getBody(String.class).contains("CONSENT_TIME_OUT_EXPIRED");
    }
}
