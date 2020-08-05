package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class FabricAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final FabricApiClient apiClient;

    public FabricAccountFetcher(FabricApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountResponse accountResponse = apiClient.fetchAccounts();
        List<AccountEntity> accounts = new ArrayList<>();
        for (AccountEntity accountEntity : accountResponse.getAccounts()) {
            AccountDetailsResponse accountDetails =
                    apiClient.getAccountDetails(accountEntity.getAccountDetailsLink());
            BalanceResponse balanceResponse =
                    apiClient.getBalances(accountEntity.getBalancesLink());
            accountDetails.setBalances(balanceResponse.getBalances());
            accounts.add(accountDetails.getAccount());
        }
        accountResponse.setAccounts(accounts);
        return accountResponse.toTinkAccounts();
    }
}
