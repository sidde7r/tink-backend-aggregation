package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class ErsteBankAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final ErsteBankApiClient ersteBankApiClient;

    public ErsteBankAccountFetcher(ErsteBankApiClient ersteBankApiClient){
        this.ersteBankApiClient = ersteBankApiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return this.ersteBankApiClient.fetchAccounts().toTransactionalAccounts();
    }
}
