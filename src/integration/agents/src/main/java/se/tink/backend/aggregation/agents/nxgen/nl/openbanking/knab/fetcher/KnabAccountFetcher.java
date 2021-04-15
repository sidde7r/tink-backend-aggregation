package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.rpc.BalancesResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class KnabAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final KnabApiClient apiClient;

    public KnabAccountFetcher(final KnabApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().getAccounts().stream()
                .map(this::getAccountWithBalance)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> getAccountWithBalance(AccountEntity account) {
        BalancesResponse balancesResponse = apiClient.fetchAccountBalance(account.getResourceId());

        return account.toTinkAccount(balancesResponse.getBalances());
    }
}
