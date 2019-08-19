package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class IngAccountsFetcher implements AccountFetcher<TransactionalAccount> {

    private final IngApiClient client;

    public IngAccountsFetcher(IngApiClient client, String currency) {
        this.client = client;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return client.fetchAccounts().getAccounts().stream()
                .map(this::enrichAccountWithBalance)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> enrichAccountWithBalance(AccountEntity account) {

        return account.toTinkAccount(client.fetchBalances(account).getBalance());
    }
}
