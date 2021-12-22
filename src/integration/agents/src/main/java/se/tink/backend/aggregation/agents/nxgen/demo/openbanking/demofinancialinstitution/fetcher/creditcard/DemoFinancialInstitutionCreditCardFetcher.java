package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.fetcher.creditcard;

import io.vavr.control.Option;
import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.DemoFinancialInstitutionApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class DemoFinancialInstitutionCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>,
                TransactionKeyPaginator<CreditCardAccount, String> {

    private final DemoFinancialInstitutionApiClient apiClient;
    private final SessionStorage sessionStorage;

    public DemoFinancialInstitutionCreditCardFetcher(
            DemoFinancialInstitutionApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.fetchAccounts().stream()
                .filter(AccountEntity::isPsd2Account)
                .map(AccountEntity::maybeToTinkCreditCardAccount)
                .flatMap(Option::toJavaStream)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            CreditCardAccount account, String nextPageToken) {
        final String accountNumber = account.getAccountNumber();

        return Option.of(nextPageToken)
                .fold(
                        () -> apiClient.fetchTransactions(accountNumber),
                        s -> apiClient.fetchTransactions(accountNumber, s));
    }
}
