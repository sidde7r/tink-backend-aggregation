package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class DemobankCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionDatePaginator<CreditCardAccount> {
    private final DemobankApiClient apiClient;

    public DemobankCreditCardFetcher(DemobankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.fetchAccounts().stream()
                .map(
                        accountEntity ->
                                accountEntity.toTinkCreditCardAccount(
                                        apiClient.fetchAccountHolders(accountEntity.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {
        return apiClient.fetchTransactions(account.getApiIdentifier(), fromDate, toDate);
    }
}
