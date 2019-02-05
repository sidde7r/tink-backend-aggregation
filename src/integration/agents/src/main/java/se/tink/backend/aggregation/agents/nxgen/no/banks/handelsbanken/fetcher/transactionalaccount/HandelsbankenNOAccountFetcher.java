package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.rpc.AccountFetchingResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class HandelsbankenNOAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final HandelsbankenNOApiClient apiClient;

    public HandelsbankenNOAccountFetcher(HandelsbankenNOApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountFetchingResponse accountFetchingResponse = apiClient.fetchAccounts();

        return accountFetchingResponse.toTinkAccounts();
    }
}

