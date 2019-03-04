package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher;

import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

import java.util.Collection;

public final class AxaAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final AxaStorage storage;
    private final AxaApiClient apiClient;

    public AxaAccountFetcher(final AxaApiClient apiClient, final AxaStorage storage) {
        this.apiClient = apiClient;
        this.storage = storage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final int customerId = storage.getCustomerId().orElseThrow(IllegalStateException::new);
        final String accessToken = storage.getAccessToken().orElseThrow(IllegalStateException::new);

        final GetAccountsResponse response = apiClient.postGetAccounts(customerId, accessToken);

        return response.getAccounts();
    }
}
