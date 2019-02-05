package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.interfaces;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConfiguration;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public interface EuroInformationApiClientFactory {
    <T extends EuroInformationApiClient> T getApiClient(TinkHttpClient client,
            SessionStorage sessionStorage,
            EuroInformationConfiguration config);
}
