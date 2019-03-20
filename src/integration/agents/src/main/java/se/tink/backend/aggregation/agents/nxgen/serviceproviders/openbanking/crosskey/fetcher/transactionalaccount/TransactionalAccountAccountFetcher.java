package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.account.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.common.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc.CrosskyeAccountBalancesResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.CheckingBuildStep;
import se.tink.libraries.account.identifiers.TinkIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionalAccountAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final CrosskeyBaseApiClient apiClient;

    public TransactionalAccountAccountFetcher(CrosskeyBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        return apiClient.fetchAccounts()
            .getData().getAccounts().stream()
            .filter(AccountEntity::isCheckingAccount)
            .map(this::toCheckingAccount)
            .collect(Collectors.toList());
    }

    protected CheckingAccount toCheckingAccount(AccountEntity accountEntity) {

        CrosskyeAccountBalancesResponse crosskyeAccountBalancesResponse = apiClient
            .fetchAccountBalances(accountEntity.getAccountId());

        AccountDetailsEntity accountDetails = accountEntity.resolveAccountDetails();
        AmountEntity amount = crosskyeAccountBalancesResponse.getData()
            .getInterimAvailableBalance().getAmount();

        String uniqueIdentifier = accountDetails != null ?
            accountDetails.getIdentification() : accountEntity.getAccountId();

        return getCheckingAccount(accountEntity, accountDetails, amount, uniqueIdentifier);
    }

    protected CheckingAccount getCheckingAccount(AccountEntity accountEntity,
        AccountDetailsEntity accountDetails, AmountEntity amount, String uniqueIdentifier) {

        CheckingBuildStep checkingBuildStep = CheckingAccount.builder()
            .setUniqueIdentifier(uniqueIdentifier)
            .setAccountNumber(uniqueIdentifier)
            .setBalance(new Amount(amount.getCurrency(), amount.getAmount()))
            .addAccountIdentifier(new TinkIdentifier(uniqueIdentifier))
            .setApiIdentifier(accountEntity.getAccountId())
            .setProductName(accountEntity.getDescription());

        return accountDetails == null ?
            checkingBuildStep.build() :
            checkingBuildStep.addHolderName(accountDetails.getName()).build();

    }
}
