package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator;

import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.BuddybankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public class BuddybankAuthenticator {

    private final BuddybankApiClient apiClient;

    public BuddybankAuthenticator(BuddybankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public URL buildAuthorizeUrl(String state) {
        return apiClient.buildAuthorizeUrl(state);
    }

    public ConsentStatusResponse getConsentStatus() {
        return apiClient.getConsentStatus();
    }
}
