package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.creditcardaccount;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.account.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.common.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc.CrosskeyAccountBalancesResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.Amount;

public class CreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {

    private final CrosskeyBaseApiClient apiClient;

    public CreditCardAccountFetcher(CrosskeyBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.fetchAccounts()
            .getData().getAccounts().stream()
            .filter(AccountEntity::isCreditCardAccount)
            .map(this::toCreditCardAccount)
            .collect(Collectors.toList());
    }

    protected CreditCardAccount toCreditCardAccount(AccountEntity accountEntity) {

        CrosskeyAccountBalancesResponse crosskeyAccountBalancesResponse = apiClient
            .fetchAccountBalances(accountEntity.getAccountId());

        AccountDetailsEntity accountDetails = accountEntity.resolveAccountDetails();
        AmountEntity amount = crosskeyAccountBalancesResponse.getData()
            .getInterimAvailableBalance().getAmount();

        String uniqueIdentifier = accountDetails != null ?
            accountDetails.getIdentification() : accountEntity.getAccountId();

        return getCreditCardAccount(accountEntity, accountDetails, amount, uniqueIdentifier);
    }

    protected CreditCardAccount getCreditCardAccount(AccountEntity accountEntity,
        AccountDetailsEntity accountDetails, AmountEntity amount, String uniqueIdentifier) {
        return CreditCardAccount.builder(uniqueIdentifier)
            .setAccountNumber(uniqueIdentifier)
            .setBalance(new Amount(amount.getCurrency(), amount.getAmount()))
            .setBankIdentifier(accountEntity.getAccountId())//TODO To apiIdentifier once possible
            .build();
    }
}
