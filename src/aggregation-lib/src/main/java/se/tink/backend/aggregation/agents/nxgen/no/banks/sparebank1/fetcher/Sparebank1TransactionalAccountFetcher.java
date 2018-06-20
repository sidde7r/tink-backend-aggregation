package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class Sparebank1TransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final Sparebank1ApiClient apiClient;
    private SessionStorage sessionStorage;

    public Sparebank1TransactionalAccountFetcher(Sparebank1ApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        List<AccountEntity> accountEntityList = apiClient.fetchAccounts().getAccounts();

        saveTransactionUrlsToSessionStorage(accountEntityList);

        return accountEntityList.stream()
                .map(AccountEntity::toTransactionalAccount)
                .collect(Collectors.toList());
    }

    private void saveTransactionUrlsToSessionStorage(List<AccountEntity> accountEntityList) {
        Map<String, String> transactionUrlsByAccountId = accountEntityList.stream().collect(Collectors.toMap(
                AccountEntity::getId, ae -> ae.getLinks().get(Sparebank1Constants.Keys.TRANSACTIONS_KEY).getHref()));
        sessionStorage.put(Sparebank1Constants.Keys.ACCOUNT_TRANSACTION_URLS_KEY, transactionUrlsByAccountId);
    }
}
