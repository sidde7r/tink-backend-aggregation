package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SBABApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities.PersonalAccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SBABAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final SBABApiClient apiClient;

    public SBABAccountFetcher(SBABApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final AccountsResponse accountsResponse = apiClient.fetchAccounts();

        return accountsResponse.getAccounts().getPersonalAccounts().stream()
                .map(PersonalAccountsEntity::toTinkTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
