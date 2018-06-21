package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.accounts;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.accounts.entities.SavingsAccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class VolvoFinansTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final VolvoFinansApiClient apiClient;

    public VolvoFinansTransactionalAccountFetcher(VolvoFinansApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return Arrays
                .stream(apiClient.savingsAccounts())
                .map(SavingsAccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }
}
