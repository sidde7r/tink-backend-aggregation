package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.RaiffeisenApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class RaiffeisenAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final RaiffeisenApiClient client;

    public RaiffeisenAccountFetcher(RaiffeisenApiClient client) {
        this.client = client;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        try {
            return client.fetchAccounts().toTransactionalAccounts();
        } catch (Exception e) {
            // If we get here the user has not consented that we access their balance
            return Collections.emptyList();
        }
    }
}
