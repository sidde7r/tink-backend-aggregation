package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.IdentificationType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.account.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.accountbalances.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.accountbalances.AccountBalancesDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc.CrosskeyAccountBalancesResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class CrossKeyTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final CrosskeyBaseApiClient apiClient;

    public CrossKeyTransactionalAccountFetcher(CrosskeyBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().getData().getAccounts().stream()
                .filter(AccountEntity::isCheckingAccount)
                .map(this::toCheckingAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> toCheckingAccount(AccountEntity accountEntity) {
        final CrosskeyAccountBalancesResponse crosskeyAccountBalancesResponse =
                apiClient.fetchAccountBalances(accountEntity.getAccountId());

        final AccountBalancesDataEntity balances = crosskeyAccountBalancesResponse.getData();

        return getCheckingAccount(accountEntity, balances);
    }

    private Optional<TransactionalAccount> getCheckingAccount(
            AccountEntity accountEntity, AccountBalancesDataEntity balances) {

        final String accountNumber =
                accountEntity
                        .getAccountDetails(IdentificationType.IBAN)
                        .map(AccountDetailsEntity::getIdentification)
                        .orElse(accountEntity.getAccountId());

        final String accountName =
                accountEntity
                        .getAccountDetails(IdentificationType.IBAN)
                        .map(AccountDetailsEntity::getName)
                        .orElse("");

        Optional<ExactCurrencyAmount> balance = getBalance(balances);

        if (!balance.isPresent()) {
            return Optional.empty();
        }

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(balance.get()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(accountNumber)
                                .withAccountName(accountEntity.getDescription())
                                .addIdentifier(new IbanIdentifier(accountNumber))
                                .setProductName(accountEntity.getDescription())
                                .build())
                .addHolderName(accountName)
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, accountEntity.getAccountId())
                .setApiIdentifier(accountEntity.getAccountId())
                .setBankIdentifier(accountEntity.getAccountId())
                .build();
    }

    private Optional<ExactCurrencyAmount> getBalance(AccountBalancesDataEntity balances) {
        Optional<AccountBalanceEntity> interimBookedBalance = balances.getInterimBookedBalance();
        if (interimBookedBalance.isPresent()) {
            return interimBookedBalance.map(AccountBalanceEntity::getExactAmount);
        }
        return balances.getInterimAvailableBalance().map(AccountBalanceEntity::getExactAmount);
    }
}
