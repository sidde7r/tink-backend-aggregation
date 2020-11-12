package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.transactionalaccount;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.IdentificationType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.account.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.common.AmountEntity;
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
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> toCheckingAccount(AccountEntity accountEntity) {
        final CrosskeyAccountBalancesResponse crosskeyAccountBalancesResponse =
                apiClient.fetchAccountBalances(accountEntity.getAccountId());

        final Optional<AccountDetailsEntity> accountDetails =
                accountEntity.getAccountDetails(IdentificationType.IBAN);
        final AmountEntity amount =
                crosskeyAccountBalancesResponse.getData().getInterimAvailableBalance().getAmount();

        return getCheckingAccount(accountEntity, accountDetails, amount);
    }

    private Optional<TransactionalAccount> getCheckingAccount(
            AccountEntity accountEntity,
            Optional<AccountDetailsEntity> accountDetails,
            AmountEntity amount) {

        final String accountNumber =
                accountDetails
                        .map(AccountDetailsEntity::getIdentification)
                        .orElse(accountEntity.getAccountId());

        final String accountName = accountDetails.map(AccountDetailsEntity::getName).orElse("");

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getBalance(amount)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(accountNumber)
                                .withAccountName(accountName)
                                .addIdentifier(new IbanIdentifier(accountNumber))
                                .setProductName(accountEntity.getDescription())
                                .build())
                .addHolderName(accountName)
                .putInTemporaryStorage(StorageKeys.ACCOUNT_ID, accountEntity.getAccountId())
                .setApiIdentifier(accountEntity.getAccountId())
                .setBankIdentifier(accountEntity.getAccountId())
                .build();
    }

    private ExactCurrencyAmount getBalance(AmountEntity amount) {
        return new ExactCurrencyAmount(
                BigDecimal.valueOf(amount.getAmount()), amount.getCurrency());
    }
}
