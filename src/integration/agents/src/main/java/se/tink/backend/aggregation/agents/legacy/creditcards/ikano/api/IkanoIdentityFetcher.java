package se.tink.backend.aggregation.agents.creditcards.ikano.api;

import java.util.NoSuchElementException;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.engagements.CardEntity;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

class IkanoIdentityFetcher {

    private final IkanoApiClient apiClient;
    private final Credentials credentials;

    IkanoIdentityFetcher(IkanoApiClient apiClient, Credentials credentials) {
        this.apiClient = apiClient;
        this.credentials = credentials;
    }

    public FetchIdentityDataResponse fetchIdentityData() {
        try {
            return apiClient.getResponse().getCards().stream()
                    .map(CardEntity::getCustomerName)
                    .distinct()
                    .map(name -> SeIdentityData.of(name, credentials.getField(Key.USERNAME)))
                    .reduce(IdentityData::throwingMerger)
                    .map(FetchIdentityDataResponse::new)
                    .orElseThrow(NoSuchElementException::new);

        } catch (LoginException e) {
            throw new IllegalStateException(
                    "Fetching identity gave empty card list: " + e.getMessage());
        }
    }
}
