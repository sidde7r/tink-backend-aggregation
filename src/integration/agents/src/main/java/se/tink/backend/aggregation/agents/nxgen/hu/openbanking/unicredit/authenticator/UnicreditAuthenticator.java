package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class UnicreditAuthenticator {

    private final UnicreditPersistentStorage unicreditStorage;
    private final UnicreditBaseApiClient apiClient;

    public UnicreditAuthenticator(
            UnicreditPersistentStorage unicreditStorage, UnicreditBaseApiClient apiClient) {
        this.unicreditStorage = unicreditStorage;
        this.apiClient = apiClient;
    }

    public URL buildAuthorizeUrl(String state) {
        ConsentResponse consentResponse = apiClient.createConsent(state);
        unicreditStorage.saveConsentId(consentResponse.getConsentId());
        return apiClient.getScaRedirectUrlFromConsentResponse(consentResponse);
    }

    public ConsentStatusResponse getConsentStatus() throws SessionException {
        return apiClient.getConsentStatus();
    }
}
