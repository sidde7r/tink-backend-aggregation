package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.transactionalaccount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.refresh.AccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.BoursoramaConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity.CategoriesEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;

public class BoursoramaTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final BoursoramaApiClient apiClient;

    public BoursoramaTransactionalAccountFetcher(BoursoramaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        ListAccountsResponse accountSummaryEntities = apiClient.getAccounts();
        if (Objects.isNull(accountSummaryEntities)) {
            throw new AccountRefreshException("Account summaries is null!");
        }
        if (accountSummaryEntities.size() < 3) {
            throw new AccountRefreshException(
                    String.format(
                            "Expected 3 accounts summaries, but got only %d",
                            accountSummaryEntities.size()));
        }
        // [index 0]: Pending accounts
        // [index 1]: Business accounts
        // [index 2]: Personal accounts
        List<TransactionalAccount> checkingAccounts =
                accountSummaryEntities.get(2).getCategories().stream()
                        .filter(CategoriesEntity::isCheckingAccounts)
                        .flatMap(categoriesEntity -> categoriesEntity.getBanks().stream())
                        .flatMap(banksEntity -> banksEntity.getAccounts().stream())
                        .map(this::toTinkCheckingAccount)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

        List<TransactionalAccount> savingsAccounts =
                accountSummaryEntities.get(2).getCategories().stream()
                        .filter(CategoriesEntity::isSavingsAccounts)
                        .flatMap(categoriesEntity -> categoriesEntity.getBanks().stream())
                        .flatMap(banksEntity -> banksEntity.getAccounts().stream())
                        .map(this::toTinkSavingsAccount)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

        List<TransactionalAccount> accounts = new ArrayList<>();
        accounts.addAll(checkingAccounts);
        accounts.addAll(savingsAccounts);
        return accounts;
    }

    private Optional<TransactionalAccount> toTinkAccount(
            AccountsEntity accountsEntity, TransactionalAccountType transactionalAccountType) {
        // Don't aggregate external accounts.
        // https://tink.slack.com/archives/CB12SB8DV/p1590681074156900
        if (accountsEntity.isExternalAccount()) {
            return Optional.empty();
        }

        String iban = accountsEntity.getIban();
        return TransactionalAccount.nxBuilder()
                .withType(transactionalAccountType)
                .withoutFlags()
                .withBalance(accountsEntity.getTinkBalance())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(accountsEntity.getAccountName())
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .putInTemporaryStorage(
                        BoursoramaConstants.Storage.ACCOUNT_KEY, accountsEntity.getAccountKey())
                .build();
    }

    private Optional<TransactionalAccount> toTinkCheckingAccount(AccountsEntity accountsEntity) {
        return toTinkAccount(accountsEntity, TransactionalAccountType.CHECKING);
    }

    private Optional<TransactionalAccount> toTinkSavingsAccount(AccountsEntity accountsEntity) {
        return toTinkAccount(accountsEntity, TransactionalAccountType.SAVINGS);
    }
}
