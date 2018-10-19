package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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
        AccountEntity topUpAccount = (topUpAccountEntities.stream()
                .filter(accountEntity -> accountEntity.getCurrency()
                        .equalsIgnoreCase(RevolutConstants.Storage.CURRENCY))
                .findFirst()
                .orElse(null));
        WalletEntity wallet = apiClient.fetchWallet();
        String requiredReference = wallet.getRef();

        Collection<TransactionalAccount> transactionalAccounts = wallet.getPockets().stream().filter(pocketEntity -> isActive(pocketEntity))
                .filter(pocketEntity -> sameCurrencyAsBaseCurrency(pocketEntity, wallet))
                .map(pocketEntity -> convertToTinkAccount(pocketEntity, requiredReference, topUpAccount))
                .collect(Collectors.toList());

        return transactionalAccounts;
    }

    private TransactionalAccount convertToTinkAccount(PocketEntity pocket, String requiredReference, AccountEntity topUpAccount) {
        // Local and global account, some pockets only have a global account
        if (pocket.isClosed()) {
            return null;
        } else {
            return pocket.toTinkAccount(requiredReference);
        }
    }

    private boolean isActive(PocketEntity pocket) {
        return RevolutConstants.Accounts.ACTIVE_STATE.equalsIgnoreCase(pocket.getState());
    }

    private boolean sameCurrencyAsBaseCurrency(PocketEntity pocket, WalletEntity walletEntity) {
        return walletEntity.getBaseCurrency().equalsIgnoreCase(pocket.getCurrency());
    }
}
