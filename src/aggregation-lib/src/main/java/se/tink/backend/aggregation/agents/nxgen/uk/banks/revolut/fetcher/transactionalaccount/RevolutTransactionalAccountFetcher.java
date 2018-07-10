package se.tink.backend.aggregation.agents.nxgen.uk.banks.revolut.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.revolut.RevolutApiClient;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.revolut.RevolutConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.revolut.entities.PocketEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.revolut.entities.WalletEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.revolut.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.revolut.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class RevolutTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final RevolutApiClient apiClient;

    public RevolutTransactionalAccountFetcher(RevolutApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        WalletEntity wallet = apiClient.fetchWallet();

        if (wallet == null) {
           return Collections.emptyList();
        }

        AccountsResponse accountsResponse = apiClient.fetchAccounts();

        return wallet.getPockets().stream()
                .filter(this::isActive)
                .map(pocket -> convertToTinkAccount(pocket, accountsResponse))
                .collect(Collectors.toList());
    }

    private TransactionalAccount convertToTinkAccount(PocketEntity pocket, AccountsResponse accountsResponse) {
        // Local and global account, some pockets only have a global account
        List<AccountEntity> accountsWithSameCurrencyAsPocket = accountsResponse.stream()
                .filter(accountEntity -> sameCurrencyAsPocket(pocket, accountEntity))
                .collect(Collectors.toList());

        if (accountsWithSameCurrencyAsPocket.size() == 1) {
            return pocket.toTinkAccount(accountsWithSameCurrencyAsPocket.get(0));
        } else if (accountsWithSameCurrencyAsPocket.size() == 2) {
            return pocket.toTinkAccount(accountsWithSameCurrencyAsPocket);
        }

        throw new IllegalStateException(
                "There should be 1 or 2 accounts matching the pocket, we can't handle other cases.");
    }

    private boolean isActive(PocketEntity pocket) {
        return RevolutConstants.Accounts.ACTIVE_STATE.equalsIgnoreCase(pocket.getState());
    }

    private boolean sameCurrencyAsPocket(PocketEntity pocket, AccountEntity accountEntity) {
        return accountEntity.getCurrency().equalsIgnoreCase(pocket.getCurrency());
    }
}
