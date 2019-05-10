package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.identitydata;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.rpc.IdentityDataResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

public class CrossKeyIdentityDataFetcher implements IdentityDataFetcher {

    private final CrossKeyApiClient apiClient;
    private final CrossKeyConfiguration config;

    public CrossKeyIdentityDataFetcher(
            final CrossKeyApiClient apiClient, final CrossKeyConfiguration config) {
        this.apiClient = apiClient;
        this.config = config;
    }

    @Override
    public IdentityData fetchIdentityData() {
        IdentityDataResponse resp = apiClient.fetchIdentityData();
        if (resp.isFailure()) {
            return null;
        }
        return config.parseIdentityData(resp);
    }
}
