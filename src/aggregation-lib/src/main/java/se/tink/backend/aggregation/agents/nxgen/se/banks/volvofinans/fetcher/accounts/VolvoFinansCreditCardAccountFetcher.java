package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.accounts;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.accounts.entities.CreditCardAccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;

public class VolvoFinansCreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {

    private final VolvoFinansApiClient apiClient;

    public VolvoFinansCreditCardAccountFetcher(VolvoFinansApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return Arrays
                .stream(apiClient.creditCardAccounts())
                .map(CreditCardAccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }
}
