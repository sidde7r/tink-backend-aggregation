package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.Transactions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenAccessor;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
public abstract class Xs2aDevelopersBaseAccountFetcher<A extends Account>
        implements AccountFetcher<A>, TransactionDatePaginator<A> {

    protected final Xs2aDevelopersApiClient apiClient;
    private final OAuth2TokenAccessor oAuth2TokenAccessor;

    @Override
    public Collection<A> fetchAccounts() {
        GetAccountsResponse getAccountsResponse = apiClient.getAccounts();

        return getAccountsResponse.getAccounts().stream()
                .filter(this::accountFilterCondition)
                .map(this::setBalance)
                .map(this::transformAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(A account, Date fromDate, Date toDate) {
        try {
            return PaginatorResponseImpl.create(
                    apiClient.fetchTransactions(
                            account,
                            new java.sql.Date(fromDate.getTime()).toLocalDate(),
                            new java.sql.Date(toDate.getTime()).toLocalDate()));
        } catch (HttpResponseException e) {
            if (isNoMoreTransactionsAvailableToFetchException(e)) {
                return PaginatorResponseImpl.createEmpty(false);
            } else if (isConsentTimeoutException(e)) {
                oAuth2TokenAccessor.invalidate();
                throw SessionError.SESSION_EXPIRED.exception();
            }
            throw e;
        }
    }

    protected abstract boolean accountFilterCondition(AccountEntity accountEntity);

    protected abstract Optional<A> transformAccount(AccountEntity accountEntity);

    private AccountEntity setBalance(AccountEntity accountEntity) {
        accountEntity.setBalance(apiClient.getBalance(accountEntity).getBalances());
        return accountEntity;
    }

    private boolean isNoMoreTransactionsAvailableToFetchException(HttpResponseException ex) {
        int status = ex.getResponse().getStatus();
        return status == Transactions.ERROR_CODE_MAX_ACCESS_EXCEEDED
                || status == Transactions.ERROR_CODE_SERVICE_UNAVAILABLE
                || status == Transactions.ERROR_CODE_CONSENT_INVALID;
    }

    private boolean isConsentTimeoutException(HttpResponseException ex) {
        return ex.getResponse().getStatus() == 400
                && ex.getResponse().getBody(String.class).contains("CONSENT_TIME_OUT_EXPIRED");
    }
}
