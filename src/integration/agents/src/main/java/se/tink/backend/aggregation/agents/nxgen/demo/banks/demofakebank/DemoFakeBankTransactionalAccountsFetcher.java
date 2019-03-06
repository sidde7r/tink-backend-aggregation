package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank;

import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

public class DemoFakeBankTransactionalAccountsFetcher implements AccountFetcher<TransactionalAccount>,
        TransactionKeyPaginator<TransactionalAccount, Date> {

    private final DemoFakeBankApiClient apiClient;
    private final DemoFakeBankTransactionFetcher demoFakeBankTransactionFetcher;

    public DemoFakeBankTransactionalAccountsFetcher(DemoFakeBankApiClient apiClient) {
        this.apiClient = apiClient;
        this.demoFakeBankTransactionFetcher = new DemoFakeBankTransactionFetcher();
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        // fetch accounts from bank
        FakeAccounts fakeAccounts = apiClient.fetchAccounts();
        // map bank internal account model to tink account & return
        return fakeAccounts.getAccounts().stream()
                .map(FakeAccount::toTinkCheckingAccount)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<Date> getTransactionsFor(TransactionalAccount account, Date key) {
        return demoFakeBankTransactionFetcher.fetchTransactionsFor(account, key);
    }
}
