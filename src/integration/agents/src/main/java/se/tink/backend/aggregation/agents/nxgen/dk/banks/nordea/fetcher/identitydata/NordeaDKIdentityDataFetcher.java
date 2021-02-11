package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.identitydata;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

@AllArgsConstructor
public class NordeaDKIdentityDataFetcher implements IdentityDataFetcher {
    private final NordeaDkApiClient nordeaClient;

    @Override
    public IdentityData fetchIdentityData() {
        return nordeaClient.fetchIdentityData().toTinkIdentityData();
    }
}
