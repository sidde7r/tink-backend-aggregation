package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.Storage;

public class SebTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final SebApiClient apiClient;
    private final Storage instanceStorage;

    public SebTransactionalAccountFetcher(SebApiClient apiClient, Storage instanceStorage) {
        this.apiClient = apiClient;
        this.instanceStorage = instanceStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        Collection<TransactionalAccount> accounts = apiClient.fetchAccounts().toTinkAccounts();
        storeAccountProductMapping(accounts);
        return accounts;
    }

    private void storeAccountProductMapping(Collection<TransactionalAccount> accounts) {
        Map<String, String> accountProductMap = new HashMap<>();
        accounts.forEach(
                account -> {
                    String accountNumber = account.getAccountNumber();
                    String accountProduct =
                            account.getFromTemporaryStorage(SebConstants.Storage.PRODUCT);
                    accountProductMap.put(accountNumber, accountProduct);
                });
        instanceStorage.put(SebConstants.Storage.ACCOUNT_PRODUCT_MAP, accountProductMap);
    }
}
