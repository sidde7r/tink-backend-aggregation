package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.entities.PocketEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount.rpc.WalletResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.rpc.BaseUserResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class RevolutTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final RevolutApiClient apiClient;

    public RevolutTransactionalAccountFetcher(RevolutApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        final AccountsResponse topUpAccountEntities = apiClient.fetchAccounts();
        final BaseUserResponse user = apiClient.fetchUser();
        final WalletResponse wallet = apiClient.fetchWallet();
        final String holderName = user.getUser().getFullName();

        // Most currencies will share iban.
        // Some currencies are handled externally, these pockets have their own iban.
        final Map<String, String> currencyIbanMap =
                topUpAccountEntities.stream()
                        .filter(account -> account.getIban().isPresent())
                        .collect(
                                Collectors.toMap(
                                        AccountEntity::getCurrency,
                                        account -> account.getIban().get(),
                                        (account1, account2) -> account1));

        return wallet.getPockets().stream()
                .filter(p -> p.isActive() && p.isOpen() && (!p.isCryptoCurrency()))
                .filter(this::isTransactionalAccount)
                .map(
                        pocket ->
                                pocket.toTransactionalAccount(
                                        currencyIbanMap.get(pocket.getCurrency()), holderName))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private boolean isTransactionalAccount(PocketEntity pocket) {
        return RevolutConstants.ACCOUNT_TYPE_MAPPER
                .translate(pocket.getType())
                .filter(AccountTypes.CHECKING::equals)
                .isPresent();
    }
}
