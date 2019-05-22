package se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.fetchers.transactional;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.BunqBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.BunqBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.fetchers.transactional.rpc.AccountsResponseWrapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BunqTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final SessionStorage sessionStorage;
    private final BunqBaseApiClient apiClient;

    public BunqTransactionalAccountFetcher(
            SessionStorage sessionStorage, BunqBaseApiClient apiClient) {
        this.sessionStorage = sessionStorage;
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountsResponseWrapper listAccounts =
                apiClient.listAccounts(sessionStorage.get(BunqBaseConstants.StorageKeys.USER_ID));
        return listAccounts.toTinkAccounts();
    }
}
