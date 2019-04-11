package se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.interfaces.EuroInformationApiClientFactory;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CreditMutuelApiClientFactory implements EuroInformationApiClientFactory {
    @Override
    public CreditMutuelApiClient getApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            EuroInformationConfiguration config) {
        return new CreditMutuelApiClient(client, sessionStorage, config);
    }
}
