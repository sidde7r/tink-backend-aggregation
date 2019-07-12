package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit.authenticator;

import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit.UnicreditApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public class UnicreditAuthenticator {

    private final UnicreditApiClient apiClient;

    public UnicreditAuthenticator(UnicreditApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public URL buildAuthorizeUrl(String state) {
        return apiClient.buildAuthorizeUrl(state);
    }

    public ConsentStatusResponse getConsentStatus() {
        return apiClient.getConsentStatus();
    }
}
