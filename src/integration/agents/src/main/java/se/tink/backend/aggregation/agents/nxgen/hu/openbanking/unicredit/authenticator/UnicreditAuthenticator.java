package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class UnicreditAuthenticator {

    private final UnicreditBaseApiClient apiClient;

    public UnicreditAuthenticator(UnicreditBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public URL buildAuthorizeUrl(String state) {
        return apiClient.buildAuthorizeUrl(state);
    }

    public ConsentStatusResponse getConsentStatus() throws SessionException {
        return apiClient.getConsentStatus();
    }
}
