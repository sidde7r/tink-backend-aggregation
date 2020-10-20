package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.FormValues;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class BecAuthenticator {
    private final BecApiClient apiClient;

    public BecAuthenticator(BecApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public URL authenticate(String state) {
        return new URL(apiClient.getConsent(state).getScaRedirect());
    }

    boolean isStoredConsentValid() {
        return FormValues.VALID.equalsIgnoreCase(apiClient.getConsentStatus().getConsentStatus());
    }
}
