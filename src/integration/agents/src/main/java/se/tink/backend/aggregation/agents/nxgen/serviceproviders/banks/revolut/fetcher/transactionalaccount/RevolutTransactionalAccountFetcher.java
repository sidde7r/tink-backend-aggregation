package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.entities.PocketEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.entities.WalletEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.rpc.BaseUserResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RevolutTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final RevolutApiClient apiClient;

    public RevolutTransactionalAccountFetcher(RevolutApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        final AccountsResponse topUpAccountEntities = apiClient.fetchAccounts();
        final BaseUserResponse user = apiClient.fetchUser();
        final WalletEntity wallet = user.getWallet();
        final String holderName = user.getUser().getFullName();

        // Most currencies will share iban.
        // Some currencies are handled externally, these pockets have their own iban.
        Map<String, String> currencyIbanMap = new HashMap<>();
        for (AccountEntity account : topUpAccountEntities) {
            account.getIban().ifPresent(iban -> currencyIbanMap.put(account.getCurrency(), iban));
        }

        return wallet.getPockets()
                .stream()
                .filter(PocketEntity::isActive)
                .filter(PocketEntity::isOpen)
                .map(
                        pocket ->
                                toTinkAccount(
                                        pocket,
                                        currencyIbanMap.get(pocket.getCurrency()),
                                        holderName))
                .collect(Collectors.toList());
    }

    private static TransactionalAccount toTinkAccount(
            PocketEntity pocket, String accountNumber, String holderName) {

        Optional<AccountTypes> accountType =
                RevolutConstants.ACCOUNT_TYPE_MAPPER.translate(pocket.getType());

        return accountType
                .map(
                        accountTypes ->
                                accountTypes == AccountTypes.CHECKING
                                        ? pocket.toTinkCheckingAccount(accountNumber, holderName)
                                        : pocket.toTinkSavingsAccount(holderName))
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Could not construct account from pocket."));
    }
}
