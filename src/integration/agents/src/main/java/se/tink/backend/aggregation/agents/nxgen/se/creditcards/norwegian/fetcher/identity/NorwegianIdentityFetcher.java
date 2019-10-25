package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.identity;

import java.util.NoSuchElementException;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.NorwegianApiClient;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.identitydata.countries.SeIdentityData;

public class NorwegianIdentityFetcher {

    private NorwegianApiClient apiClient;
    private CredentialsRequest request;

    public NorwegianIdentityFetcher(NorwegianApiClient apiClient, CredentialsRequest request) {
        this.apiClient = apiClient;
        this.request = request;
    }

    public FetchIdentityDataResponse fetchIdentityData() {
        String identityPage = apiClient.fetchIdentityPage();
        String ssn = request.getCredentials().getField(Key.USERNAME);

        return NorwegianIdentityUtils.parseAccountName(identityPage)
                .map(name -> SeIdentityData.of(name, ssn))
                .map(FetchIdentityDataResponse::new)
                .orElseThrow(NoSuchElementException::new);
    }
}
