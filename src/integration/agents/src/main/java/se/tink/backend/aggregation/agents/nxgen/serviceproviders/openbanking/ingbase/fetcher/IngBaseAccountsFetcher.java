package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class IngBaseAccountsFetcher implements AccountFetcher<TransactionalAccount> {

    private final IngBaseApiClient client;
    private final String currency;
    private final boolean shouldReturnLowercaseAccountId;

    public IngBaseAccountsFetcher(
            IngBaseApiClient client, String currency, boolean shouldReturnLowercaseAccountId) {
        this.client = client;
        this.currency = currency;
        this.shouldReturnLowercaseAccountId = shouldReturnLowercaseAccountId;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return client.fetchAccounts().getTransactionalAccounts(currency).stream()
                .map(this::enrichAccountWithBalance)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> enrichAccountWithBalance(AccountEntity account) {
        return account.toTinkAccount(
                client.fetchBalances(account).getBalances(), shouldReturnLowercaseAccountId);
    }
}
