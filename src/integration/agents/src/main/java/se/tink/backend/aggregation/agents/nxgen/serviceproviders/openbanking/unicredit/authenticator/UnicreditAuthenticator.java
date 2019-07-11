package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class UnicreditAuthenticator {

    private final UnicreditBaseApiClient apiClient;

    public UnicreditAuthenticator(UnicreditBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public URL buildAuthorizeUrl(String state) {
        return apiClient.buildAuthorizeUrl(state);
    }

    public boolean isConsentValid() {
        return apiClient.getConsentStatus().isValidConsent();
    }
}
