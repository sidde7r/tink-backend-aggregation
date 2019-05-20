package se.tink.backend.aggregation.agents.nxgen.fi.banks.danskebank;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.danskebank.rpc.FetchHouseholdFIRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.danskebank.rpc.FetchHouseholdFIResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class DanskeBankFIApiClient extends DanskeBankApiClient {
    private static final AggregationLogger log = new AggregationLogger(DanskeBankFIApiClient.class);

    protected DanskeBankFIApiClient(
            TinkHttpClient client, DanskeBankFIConfiguration configuration) {
        super(client, configuration);
    }

    public FetchHouseholdFIResponse fetchHousehold() {
        final FetchHouseholdFIRequest request =
                FetchHouseholdFIRequest.createFromLanguageCode(configuration.getLanguageCode());
        return postRequest(
                DanskeBankConstants.Url.HOUSEHOLD_FI, FetchHouseholdFIResponse.class, request);
    }
}
