package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.account.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.common.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc.CrosskeyAccountBalancesResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.CheckingBuildStep;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.TinkIdentifier;
import se.tink.libraries.amount.Amount;

public class TransactionalAccountAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final CrosskeyBaseApiClient apiClient;

    public TransactionalAccountAccountFetcher(CrosskeyBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().getData().getAccounts().stream()
                .filter(AccountEntity::isCheckingAccount)
                .map(this::toCheckingAccount)
                .collect(Collectors.toList());
    }

    private CheckingAccount toCheckingAccount(AccountEntity accountEntity) {
        final CrosskeyAccountBalancesResponse crosskeyAccountBalancesResponse =
                apiClient.fetchAccountBalances(accountEntity.getAccountId());

        final Optional<AccountDetailsEntity> accountDetails = accountEntity.resolveAccountDetails();
        final AmountEntity amount =
                crosskeyAccountBalancesResponse.getData().getInterimAvailableBalance().getAmount();

        return getCheckingAccount(accountEntity, accountDetails, amount);
    }

    private CheckingAccount getCheckingAccount(
            AccountEntity accountEntity,
            Optional<AccountDetailsEntity> accountDetails,
            AmountEntity amount) {

        final String accountNumber =
                accountDetails
                        .map(AccountDetailsEntity::getIdentification)
                        .orElse(accountEntity.getAccountId());

        final CheckingBuildStep checkingBuildStep =
                CheckingAccount.builder()
                        .setUniqueIdentifier(accountNumber)
                        .setAccountNumber(accountNumber)
                        .setBalance(new Amount(amount.getCurrency(), amount.getAmount()))
                        .setAlias(accountEntity.getDescription())
                        .addAccountIdentifier(new TinkIdentifier(accountEntity.getAccountId()))
                        .addAccountIdentifier(new IbanIdentifier(accountNumber))
                        .setApiIdentifier(accountEntity.getAccountId())
                        .setProductName(accountEntity.getDescription());

        return accountDetails
                .map(ad -> checkingBuildStep.addHolderName(ad.getName()).build())
                .orElse(checkingBuildStep.build());
    }
}
