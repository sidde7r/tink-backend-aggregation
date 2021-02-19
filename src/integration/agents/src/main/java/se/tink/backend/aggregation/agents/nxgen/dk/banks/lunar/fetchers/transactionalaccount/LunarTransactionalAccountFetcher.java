package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.LunarPredicates;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.client.FetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.GoalEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class LunarTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final FetcherApiClient apiClient;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        Collection<TransactionalAccount> accounts = new ArrayList<>();
        accounts.addAll(fetchCheckingAccounts());
        accounts.addAll(fetchSavingsAccounts());
        return accounts;
    }

    private Collection<TransactionalAccount> fetchCheckingAccounts() {
        return apiClient.fetchAccounts().getAccounts().stream()
                .filter(LunarPredicates.lunarAccount())
                .filter(LunarPredicates.notDeleted())
                .map(AccountEntity::toTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Collection<TransactionalAccount> fetchSavingsAccounts() {
        return apiClient.fetchGoals().getGoals().stream()
                .filter(LunarPredicates.notDeleted())
                .map(GoalEntity::toTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
