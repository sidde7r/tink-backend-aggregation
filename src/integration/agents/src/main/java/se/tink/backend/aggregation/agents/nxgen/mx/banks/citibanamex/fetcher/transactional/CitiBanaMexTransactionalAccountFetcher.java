package se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.fetcher.transactional;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.CitiBanaMexApiClient;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.CitiBanaMexConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.fetcher.transactional.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CitiBanaMexTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount> {
    private final CitiBanaMexApiClient client;
    private final SessionStorage sessionStorage;

    public CitiBanaMexTransactionalAccountFetcher(
            CitiBanaMexApiClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public List<TransactionalAccount> fetchAccounts() {
        AccountsResponse accountsResponse = client.fetchAccounts();
        return accountsResponse.toTransactionalAccounts(sessionStorage.get(Storage.HOLDER_NAME));
    }
}
