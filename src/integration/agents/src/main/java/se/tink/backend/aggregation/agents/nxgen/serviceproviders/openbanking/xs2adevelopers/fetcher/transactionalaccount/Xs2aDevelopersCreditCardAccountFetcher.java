package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@AllArgsConstructor
public class Xs2aDevelopersCreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {

    private final Xs2aDevelopersApiClient apiClient;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        GetAccountsResponse getAccountsResponse = apiClient.getAccounts();

        return getAccountsResponse.getAccounts().stream()
                .filter(AccountEntity::isCreditCardAccount)
                .map(this::transformAccount)
                .collect(Collectors.toList());
    }

    private CreditCardAccount transformAccount(AccountEntity transactionAccountEntity) {
        transactionAccountEntity.setBalance(
                apiClient.getBalance(transactionAccountEntity).getBalances());
        return transactionAccountEntity.toTinkCreditAccount();
    }
}
