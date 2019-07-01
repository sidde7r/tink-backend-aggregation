package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.identitydata;

import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

public class BankinterIdentityDataFetcher implements IdentityDataFetcher {
    final BankinterApiClient apiClient;

    public BankinterIdentityDataFetcher(BankinterApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public IdentityData fetchIdentityData() {
        return apiClient.fetchIdentityData().toIdentityData();
    }
}
