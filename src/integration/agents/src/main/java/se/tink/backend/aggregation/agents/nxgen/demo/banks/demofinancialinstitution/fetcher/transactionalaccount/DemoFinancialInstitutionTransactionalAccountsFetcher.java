package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.DemoFinancialInstitutionApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.DemoFinancialInstitutionConstants;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.transactionalaccount.entities.FakeAccount;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.transactionalaccount.rpc.DemoFinancialInstitutionAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class DemoFinancialInstitutionTransactionalAccountsFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, Date> {

    private final DemoFinancialInstitutionApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final DemoFinancialInstitutionTransactionFetcher demoFinancialInstitutionTransactionFetcher;

    public DemoFinancialInstitutionTransactionalAccountsFetcher(
            DemoFinancialInstitutionApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.demoFinancialInstitutionTransactionFetcher = new DemoFinancialInstitutionTransactionFetcher();
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        DemoFinancialInstitutionAccountsResponse demoFinancialInstitutionAccountsResponse =
                apiClient.fetchAccounts(
                        sessionStorage.get(DemoFinancialInstitutionConstants.Storage.USERNAME),
                        sessionStorage.get(DemoFinancialInstitutionConstants.Storage.AUTH_TOKEN));

        return demoFinancialInstitutionAccountsResponse.getAccounts().stream()
                .filter(FakeAccount::isTransactionalAccount)
                .map(FakeAccount::toTinkCheckingAccount)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<Date> getTransactionsFor(
            TransactionalAccount account, Date key) {
        return demoFinancialInstitutionTransactionFetcher.fetchTransactionsFor(account, key);
    }
}
