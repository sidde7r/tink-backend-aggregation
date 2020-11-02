package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.identitydata;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

public class NordeaSeIdentityDataFetcher implements IdentityDataFetcher {
    private final NordeaBaseApiClient apiClient;
    private final NordeaConfiguration nordeaConfiguration;

    public NordeaSeIdentityDataFetcher(
            final NordeaBaseApiClient apiClient, final NordeaConfiguration nordeaConfiguration) {
        this.apiClient = apiClient;
        this.nordeaConfiguration = nordeaConfiguration;
    }

    @Override
    public IdentityData fetchIdentityData() {
        return apiClient.fetchIdentityData().toTinkIdentityData(nordeaConfiguration);
    }
}
