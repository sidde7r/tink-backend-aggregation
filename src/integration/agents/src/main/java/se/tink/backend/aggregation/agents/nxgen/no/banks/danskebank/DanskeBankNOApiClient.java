package se.tink.backend.aggregation.agents.nxgen.no.banks.danskebank;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class DanskeBankNOApiClient extends DanskeBankApiClient {
    protected DanskeBankNOApiClient(
            TinkHttpClient client, DanskeBankNOConfiguration configuration) {
        super(client, configuration);
    }
}
