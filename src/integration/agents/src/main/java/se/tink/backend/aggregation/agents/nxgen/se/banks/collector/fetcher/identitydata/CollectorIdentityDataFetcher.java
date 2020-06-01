package se.tink.backend.aggregation.agents.nxgen.se.banks.collector.fetcher.identitydata;

import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.CollectorApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

public class CollectorIdentityDataFetcher implements IdentityDataFetcher {
    private final CollectorApiClient apiClient;
    private final String ssn;

    public CollectorIdentityDataFetcher(CollectorApiClient apiClient, String ssn) {
        this.apiClient = apiClient;
        this.ssn = ssn;
    }

    @Override
    public IdentityData fetchIdentityData() {
        return apiClient.fetchIdentityData().toTinkIdentityData(ssn);
    }
}
