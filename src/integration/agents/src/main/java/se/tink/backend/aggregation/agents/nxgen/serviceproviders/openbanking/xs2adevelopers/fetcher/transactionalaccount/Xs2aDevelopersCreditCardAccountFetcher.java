package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.Transactions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class Xs2aDevelopersCreditCardAccountFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionDatePaginator<CreditCardAccount> {

    private final Xs2aDevelopersApiClient apiClient;

    public Xs2aDevelopersCreditCardAccountFetcher(Xs2aDevelopersApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        GetAccountsResponse getAccountsResponse = apiClient.getAccounts();

        return getAccountsResponse.getAccountList().stream()
                .filter(AccountEntity::isCreditCardAccount)
                .map(this::transformAccount)
                .collect(Collectors.toList());
    }

    private CreditCardAccount transformAccount(AccountEntity transactionAccountEntity) {
        transactionAccountEntity.setBalance(
                apiClient.getBalance(transactionAccountEntity).getBalances());
        return transactionAccountEntity.toTinkCreditAccount();
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {
        try {
            return PaginatorResponseImpl.create(
                    apiClient
                            .getCreditTransactions(account, fromDate, toDate)
                            .toTinkTransactions());
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == Transactions.ERROR_CODE_MAX_ACCESS_EXCEEDED
                    || e.getResponse().getStatus() == Transactions.ERROR_CODE_CONSENT_INVALID) {
                return PaginatorResponseImpl.createEmpty(false);
            } else {
                throw e;
            }
        }
    }
}
