package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.savingsaccount;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.NorwegianApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.common.TransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.savingsaccount.entity.SavingsAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.savingsaccount.entity.SavingsAccountResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class NorwegianSavingsAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final NorwegianApiClient apiClient;
    private String accountNo;

    public NorwegianSavingsAccountFetcher(NorwegianApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final SavingsAccountResponse savingsAccount = apiClient.fetchSavingsAccount();

        // If the user doesn't have an account the savingsAccountResponse should be null, this
        // check is a fail safe in case they change logic on their end.
        if (savingsAccount == null || savingsAccount.getAccounts() == null) {
            return Lists.newArrayList();
        }

        return savingsAccount.getAccounts().stream()
                .map(SavingsAccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        if (accountNo == null) {
            accountNo = apiClient.fetchSavingsAccountNumber();
        }

        return TransactionFetcher.fetchTransactionsFor(apiClient, accountNo, fromDate, toDate);
    }
}
