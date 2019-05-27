package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator;

import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public class VolksbankAuthenticator {

    private final VolksbankApiClient apiClient;

    public VolksbankAuthenticator(VolksbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public URL buildAuthorizeUrl(String state) {
        return apiClient.buildAuthorizeUrl(state);
    }

    public ConsentStatusResponse getConsentStatus() {
        return apiClient.getConsentStatus();
    }

    public ConsentResponse getDetailedConsent(String state) {
        return apiClient.getDetailedConsent(state);
    }
}
