package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

// TODO: Authentication must be implemented for the production
public class BecAuthenticator {
    private final BecApiClient apiClient;
    private final SessionStorage sessionStorage;

    public BecAuthenticator(BecApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    public URL buildAuthorizeUrl(String state) {
        ConsentResponse response = apiClient.getConsent(state);
        return new URL(response.getScaRedirect());
    }

    public boolean getApprovedConsent() {
        ConsentResponse consentResponse = apiClient.getConsentStatus();
        return consentResponse.getConsentStatus().equalsIgnoreCase("valid");
    }
}
