package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.NordeaFiApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class NordeaTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final NordeaFiApiClient client;

    public NordeaTransactionalAccountFetcher(
            NordeaFiApiClient client) {
        this.client = client;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return client.fetchAccounts().toTinkAccounts();
    }
}