package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquetransatlantique;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.interfaces.ApiClientFactory;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BanqueTransatlantiqueApiClientFactory implements ApiClientFactory {
    @Override
    public BanqueTransatlantiqueApiClient getApiClient(TinkHttpClient client,
            SessionStorage sessionStorage,
            EuroInformationConfiguration config) {
        return new BanqueTransatlantiqueApiClient(client, sessionStorage, config);
    }
}
