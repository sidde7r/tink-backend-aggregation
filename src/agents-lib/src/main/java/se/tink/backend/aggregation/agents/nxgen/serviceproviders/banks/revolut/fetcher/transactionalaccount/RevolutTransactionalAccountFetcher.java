package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount;

import java.util.ArrayList;
import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.entities.PocketEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.entities.WalletEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class RevolutTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final RevolutApiClient apiClient;

    public RevolutTransactionalAccountFetcher(RevolutApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        AccountsResponse topUpAccountEntities = apiClient.fetchAccounts();
        WalletEntity wallet = apiClient.fetchWallet();
        String requiredReference = wallet.getRef();

        Collection<TransactionalAccount> transactionalAccounts = new ArrayList<>();
        for (PocketEntity pocket : wallet.getPockets()) {
            if (isActive(pocket) && !pocket.isClosed()) {
                for (AccountEntity topUpAccount : topUpAccountEntities) {
                    if (requiredReference.equalsIgnoreCase(topUpAccount.getRequiredReference())
                            && matchPocketToAccount(pocket, topUpAccount)) {
                        transactionalAccounts.add(convertToTinkAccount(pocket, requiredReference,
                                topUpAccount));
                    }
                }
            }
        }

        return transactionalAccounts;
    }

    private TransactionalAccount convertToTinkAccount(PocketEntity pocket, String requiredReference,
            AccountEntity topUpAccount) {
        return pocket.toTinkAccount(requiredReference, topUpAccount);
    }

    private boolean isActive(PocketEntity pocket) {
        return RevolutConstants.Accounts.ACTIVE_STATE.equalsIgnoreCase(pocket.getState());
    }

    private boolean matchPocketToAccount(PocketEntity pocket, AccountEntity topUpAccount) {
        return topUpAccount.getCurrency().equalsIgnoreCase(pocket.getCurrency());
    }

}
