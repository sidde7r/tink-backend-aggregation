package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataEntity;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class IdentityDataFetcher {

    private final UkOpenBankingApiClient ukOpenBankingApiClient;

    public IdentityDataFetcher(UkOpenBankingApiClient ukOpenBankingApiClient) {
        this.ukOpenBankingApiClient = ukOpenBankingApiClient;
    }

    public IdentityDataEntity fetchUserDetails(URL identityDataEndpointURL) {
        return null;
    }
}
