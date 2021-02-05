package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.Transactions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenAccessor;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@AllArgsConstructor
@Slf4j
public class Xs2aDevelopersTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final Xs2aDevelopersApiClient apiClient;
    private final OAuth2TokenAccessor oAuth2TokenAccessor;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        GetAccountsResponse getAccountsResponse = apiClient.getAccounts();

        if (getAccountsResponse.getAccounts().stream()
                .filter(a -> "CARD".equals(a.getCashAccountType()))
                .findFirst()
                .isPresent()) {
            log.info("Account type is CARD."); // MINI-572 Searching for possible CARD accounts in
            // general accounts endpoint
        }

        return getAccountsResponse.getAccounts().stream()
                .map(this::transformAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> transformAccount(AccountEntity accountEntity) {
        accountEntity.setBalance(apiClient.getBalance(accountEntity).getBalances());
        return accountEntity.toTinkAccount();
    }

    // Agents that use this should probably be moved to use Xs2aDevelopersTransactionDateFromFetcher
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
                oAuth2TokenAccessor.invalidate();
                throw SessionError.CONSENT_EXPIRED.exception(e.getMessage());
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
