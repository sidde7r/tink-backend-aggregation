package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.transactionalaccount;


import io.vavr.control.Option;
import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.DemoFinancialInstitutionApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class DemoFinancialInstitutionTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, URL> {

    private final DemoFinancialInstitutionApiClient apiClient;
    private final SessionStorage sessionStorage;

    public DemoFinancialInstitutionTransactionalAccountFetcher(
            DemoFinancialInstitutionApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().stream()
                .map(AccountEntity::maybeToTinkTransationalAccount)
                .flatMap(Option::toJavaStream)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<URL> getTransactionsFor(
            TransactionalAccount account, URL nextUrl) {
        final String accountNumber = account.getAccountNumber();

        return Option.of(nextUrl)
                .fold(
                        () -> apiClient.fetchTransactions(accountNumber),
                        s -> apiClient.fetchTransactionsForNextUrl(nextUrl));
    }
}
