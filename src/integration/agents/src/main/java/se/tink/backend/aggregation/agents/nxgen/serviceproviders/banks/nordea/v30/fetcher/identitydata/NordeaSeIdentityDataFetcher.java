package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.identitydata;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.identitydata.rpc.FetchIdentityDataResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
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
        FetchIdentityDataResponse identityDataResponse = apiClient.fetchIdentityData();
        if (nordeaConfiguration.isBusinessAgent()) {
            log.info(
                    "Business entity information: {}",
                    SerializationUtils.serializeToString(identityDataResponse));
        }
        return identityDataResponse.toTinkIdentityData(nordeaConfiguration);
    }
}
