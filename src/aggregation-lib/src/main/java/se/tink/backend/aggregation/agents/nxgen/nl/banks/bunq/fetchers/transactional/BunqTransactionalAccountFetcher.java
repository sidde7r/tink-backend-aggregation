package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.fetchers.transactional;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.BunqApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.BunqConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.fetchers.transactional.rpc.AccountsResponseWrapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.rpc.Credentials;

public class BunqTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final Credentials credentials;
    private final SessionStorage sessionStorage;
    private final BunqApiClient apiClient;

    public BunqTransactionalAccountFetcher(Credentials credentials,
            SessionStorage sessionStorage, BunqApiClient apiClient) {
        this.credentials = credentials;
        this.sessionStorage = sessionStorage;
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountsResponseWrapper listAccounts = apiClient.listAccounts(
                sessionStorage.get(BunqConstants.StorageKeys.USER_ID));
        return listAccounts.toTinkAccounts();
    }
}
