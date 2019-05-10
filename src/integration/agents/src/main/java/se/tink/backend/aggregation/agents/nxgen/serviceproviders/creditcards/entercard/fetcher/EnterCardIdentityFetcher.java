package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

public class EnterCardIdentityFetcher implements IdentityDataFetcher {

    private EnterCardApiClient apiClient;
    private Credentials credentials;

    public EnterCardIdentityFetcher(EnterCardApiClient apiClient, Credentials credentials) {
        this.apiClient = apiClient;
        this.credentials = credentials;
    }

    @Override
    public IdentityData fetchIdentityData() {
        String name = apiClient.fetchUserDetails().getUser().getName();

        return SeIdentityData.of(name, credentials.getField(Field.Key.USERNAME));
    }
}
