package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.identitydata;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

public class NordeaSeIdentityDataFetcher implements IdentityDataFetcher {
    private final NordeaSEApiClient apiClient;
    private final Credentials credentials;

    public NordeaSeIdentityDataFetcher(
            final NordeaSEApiClient apiClient, final Credentials credentials) {
        this.apiClient = apiClient;
        this.credentials = credentials;
    }

    @Override
    public IdentityData fetchIdentityData() {
        return apiClient.fetchAccount().getIdentityData(credentials);
    }
}
