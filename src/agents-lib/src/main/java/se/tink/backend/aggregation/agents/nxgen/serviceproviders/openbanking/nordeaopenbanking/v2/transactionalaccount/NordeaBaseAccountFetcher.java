package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class NordeaBaseAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final NordeaBaseApiClient apiClient;
    private final NordeaAccountParser accountParser;

    public NordeaBaseAccountFetcher(NordeaBaseApiClient apiClient, NordeaAccountParser accountParser) {
        this.apiClient = apiClient;
        this.accountParser = accountParser;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountsResponse accountsResponse = apiClient.fetchAccounts();

        Collection<TransactionalAccount> accounts = accountsResponse.getTinkAccounts(accountParser);

        return accounts;
    }
}
