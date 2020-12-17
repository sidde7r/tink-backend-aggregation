package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher.rpc.UserResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class EnterCardAccountFetcher implements AccountFetcher<CreditCardAccount> {

    private EnterCardApiClient apiClient;

    public EnterCardAccountFetcher(EnterCardApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        UserResponse response = apiClient.fetchUserDetails();

        return response.getCreditCardAccountIds().stream()
                .map(
                        accountId ->
                                apiClient
                                        .fetchCardAccount(accountId)
                                        .toCreditCardAccount(response.getUser(), accountId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
