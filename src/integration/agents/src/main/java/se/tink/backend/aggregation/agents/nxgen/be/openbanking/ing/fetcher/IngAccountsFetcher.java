package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class IngAccountsFetcher implements AccountFetcher<TransactionalAccount> {

    private final IngApiClient client;
    private final String currency;

    public IngAccountsFetcher(IngApiClient client, String currency) {
        this.client = client;
        this.currency = currency;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return client.fetchAccounts().getAccounts(currency).stream()
                .map(
                        a ->
                                a.toTinkAccount(
                                        client.fetchBalances(a).getBalanceAmount(a.getCurrency())))
                .collect(Collectors.toList());
    }
}
