package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.identitydata;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.entity.UserEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

public class SebKortIdentityDataFetcher implements IdentityDataFetcher {

    private final SebKortApiClient apiClient;
    private final Credentials credentials;

    public SebKortIdentityDataFetcher(SebKortApiClient apiClient, Credentials credentials) {
        this.apiClient = apiClient;
        this.credentials = credentials;
    }

    @Override
    public IdentityData fetchIdentityData() {
        final UserEntity user = apiClient.fetchCards().getUser();
        return SeIdentityData.of(
                user.getGivenName(), user.getBirthName(), credentials.getField(Field.Key.USERNAME));
    }
}
