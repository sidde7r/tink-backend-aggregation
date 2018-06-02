package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.entitites.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.rpc.AccountListResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SparebankenSorTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final SparebankenSorApiClient apiClient;
    private final SessionStorage sessionStorage;

    public SparebankenSorTransactionalAccountFetcher(SparebankenSorApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountListResponse accountListResponse = apiClient.fetchAccounts();
        saveTransactionUrlsToSessionStorage(accountListResponse.getAccountList());
        return accountListResponse.toTinkAccounts();
    }

    private void saveTransactionUrlsToSessionStorage(List<AccountEntity> accountEntityList) {
        Map<String, String> transactionUrlsByAccountId = accountEntityList.stream()
                .collect(Collectors.toMap(
                        AccountEntity::getId,
                        ae -> ae.getLinks().get(SparebankenSorConstants.Storage.TRANSACTIONS).getHref())
                );

        sessionStorage.put(SparebankenSorConstants.Storage.ACCOUNT_TRANSACTION_URLS, transactionUrlsByAccountId);
    }
}
