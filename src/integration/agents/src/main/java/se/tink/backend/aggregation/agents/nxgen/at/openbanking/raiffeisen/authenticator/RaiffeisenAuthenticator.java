package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator;

import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenApiClient;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;

public class RaiffeisenAuthenticator {

    private final RaiffeisenApiClient apiClient;

    public RaiffeisenAuthenticator(RaiffeisenApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public OAuth2Token fetchToken() {
        return apiClient.getToken();
    }

    public URL fetchAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(state);
    }

    public String checkConsentStatus() {
        return apiClient.getConsentStatus();
    }
}
