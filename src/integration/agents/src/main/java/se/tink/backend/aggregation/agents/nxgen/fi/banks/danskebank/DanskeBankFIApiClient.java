package se.tink.backend.aggregation.agents.nxgen.fi.banks.danskebank;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.danskebank.rpc.FetchHouseholdFIRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.danskebank.rpc.FetchHouseholdFIResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.i18n.Catalog;

public class DanskeBankFIApiClient extends DanskeBankApiClient {
    protected DanskeBankFIApiClient(
            TinkHttpClient client,
            DanskeBankFIConfiguration configuration,
            Credentials credentials,
            Catalog catalog) {
        super(client, configuration, credentials, catalog);
    }

    public FetchHouseholdFIResponse fetchHousehold() {
        final FetchHouseholdFIRequest request =
                FetchHouseholdFIRequest.createFromLanguageCode(configuration.getLanguageCode());
        return postRequest(constants.getHouseholdFiUrl(), FetchHouseholdFIResponse.class, request);
    }
}
