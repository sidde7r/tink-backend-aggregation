package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;
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

    public FetchTransactionsResponse fetchTransactions(TransactionalAccount account, String key) {
        URL url =
                Optional.ofNullable(key)
                        .map(k -> new URL(SebConstants.Urls.BASE_AIS).concat(k))
                        .orElse(
                                new URL(SebConstants.Urls.TRANSACTIONS)
                                        .parameter(
                                                SebCommonConstants.IdTags.ACCOUNT_ID,
                                                account.getApiIdentifier()));

        return apiClient.fetchTransactions(url.toString(), key == null);
    }
}
