package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SBABApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities.PersonalAccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccounts.entities.SharedAccountsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SBABAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final SBABApiClient apiClient;

    public SBABAccountFetcher(SBABApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final AccountsEntity accounts = apiClient.fetchAccounts().getAccounts();

        return Stream.concat(
                        accounts.getPersonalAccounts().stream()
                                .map(PersonalAccountsEntity::toTinkTransactionalAccount),
                        accounts.getSharedAccounts().stream()
                                .map(SharedAccountsEntity::toTinkTransactionalAccount))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
