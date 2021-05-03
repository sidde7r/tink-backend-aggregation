package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.identity;

import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.NorwegianApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.identity.rpc.ContactInfoResponse;
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
        final ContactInfoResponse contactInfo = apiClient.fetchContactInfo();
        final String ssn = request.getCredentials().getField(Key.USERNAME);

        return new FetchIdentityDataResponse(
                SeIdentityData.of(contactInfo.getFirstName(), contactInfo.getLastName(), ssn));
    }
}
