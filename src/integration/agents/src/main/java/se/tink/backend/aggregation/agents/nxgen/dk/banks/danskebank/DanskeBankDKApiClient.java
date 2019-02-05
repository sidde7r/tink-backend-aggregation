package se.tink.backend.aggregation.agents.nxgen.dk.banks.danskebank;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class DanskeBankDKApiClient extends DanskeBankApiClient {
    protected DanskeBankDKApiClient(TinkHttpClient client, DanskeBankDKConfiguration configuration) {
        super(client, configuration);
    }
}
