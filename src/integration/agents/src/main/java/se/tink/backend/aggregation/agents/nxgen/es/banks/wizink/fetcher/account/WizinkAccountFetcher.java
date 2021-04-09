package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class WizinkAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private WizinkStorage wizinkStorage;
    private WizinkApiClient wizinkApiClient;

    public WizinkAccountFetcher(WizinkApiClient wizinkApiClient, WizinkStorage wizinkStorage) {
        this.wizinkApiClient = wizinkApiClient;
        this.wizinkStorage = wizinkStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return Collections.emptyList();
    }
}
