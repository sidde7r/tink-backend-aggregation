package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.Transactions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.BalanceEntity;
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

    public Xs2aDevelopersTransactionalAccountFetcher(Xs2aDevelopersApiClient apiClient) {
        this.apiClient = apiClient;
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
        BalanceEntity balanceEntity = apiClient.getBalance(accountEntity).getBalances().get(0);
        return accountEntity.toTinkAccount(balanceEntity);
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        try {
            return PaginatorResponseImpl.create(
                    apiClient.getTransactions(account, fromDate, toDate).toTinkTransactions());
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == Transactions.ERROR_CODE_MAX_ACCESS_EXCEEDED
                    || e.getResponse().getStatus() == Transactions.ERROR_CODE_SERVICE_UNAVAILABLE
                    || e.getResponse().getStatus() == Transactions.ERROR_CODE_CONSENT_INVALID) {
                return PaginatorResponseImpl.createEmpty(false);
            } else {
                throw e;
            }
        }
    }
}
