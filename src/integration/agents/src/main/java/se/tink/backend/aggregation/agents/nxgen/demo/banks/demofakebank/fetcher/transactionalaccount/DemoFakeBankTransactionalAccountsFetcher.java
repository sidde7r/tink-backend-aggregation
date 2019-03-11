package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.DemoFakeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.DemoFakeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.fetcher.transactionalaccount.entities.FakeAccount;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.fetcher.transactionalaccount.rpc.DemoFakeBankAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

public class DemoFakeBankTransactionalAccountsFetcher implements AccountFetcher<TransactionalAccount>,
        TransactionKeyPaginator<TransactionalAccount, Date> {

    private final DemoFakeBankApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final DemoFakeBankTransactionFetcher demoFakeBankTransactionFetcher;

    public DemoFakeBankTransactionalAccountsFetcher(DemoFakeBankApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.demoFakeBankTransactionFetcher = new DemoFakeBankTransactionFetcher();
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        DemoFakeBankAccountsResponse demoFakeBankAccountsResponse = apiClient.fetchAccounts(
                sessionStorage.get(DemoFakeBankConstants.Storage.USERNAME),
                sessionStorage.get(DemoFakeBankConstants.Storage.AUTH_TOKEN));

        return demoFakeBankAccountsResponse.getAccounts().stream().filter(FakeAccount::isTransactionalAccount)
                .map(FakeAccount::toTinkCheckingAccount)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<Date> getTransactionsFor(TransactionalAccount account, Date key) {
        return demoFakeBankTransactionFetcher.fetchTransactionsFor(account, key);
    }
}
