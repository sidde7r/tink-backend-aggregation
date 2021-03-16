package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.configuration.MarketConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@AllArgsConstructor
public class IngBaseAccountsFetcher implements AccountFetcher<TransactionalAccount> {

    private final IngBaseApiClient client;
    private final String currency;
    private final MarketConfiguration marketConfiguration;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return client.fetchAccounts().getTransactionalAccounts(currency).stream()
                .map(this::toTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> toTransactionalAccount(AccountEntity account) {
        return account.toTinkAccount(
                client.fetchBalances(account).getBalances(), marketConfiguration);
    }
}
