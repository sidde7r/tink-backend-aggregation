package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.client.FabricFetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class FabricAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final PersistentStorage persistentStorage;
    private final FabricFetcherApiClient fetcherApiClient;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        String consentId = persistentStorage.get(StorageKeys.CONSENT_ID);
        AccountResponse accountResponse = fetcherApiClient.fetchAccounts(consentId);
        List<AccountEntity> accounts = new ArrayList<>();
        for (AccountEntity accountEntity : accountResponse.getAccounts()) {
            AccountDetailsResponse accountDetails =
                    fetcherApiClient.getAccountDetails(
                            consentId, accountEntity.getAccountDetailsLink());
            BalanceResponse balanceResponse =
                    fetcherApiClient.getBalances(consentId, accountEntity.getBalancesLink());
            accountDetails.setBalances(balanceResponse.getBalances());
            accounts.add(accountDetails.getAccount());
        }
        accountResponse.setAccounts(accounts);
        return accountResponse.toTinkAccounts();
    }
}
