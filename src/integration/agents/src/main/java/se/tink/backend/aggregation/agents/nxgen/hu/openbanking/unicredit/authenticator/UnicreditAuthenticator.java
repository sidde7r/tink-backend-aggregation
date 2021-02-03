package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class UnicreditAuthenticator {

    private final UnicreditStorage unicreditStorage;
    private final UnicreditBaseApiClient apiClient;

    public URL buildAuthorizeUrl(String state) {
        ConsentResponse consentResponse = apiClient.createConsent(state);
        unicreditStorage.saveConsentId(consentResponse.getConsentId());
        return apiClient.getScaRedirectUrlFromConsentResponse(consentResponse);
    }

    public ConsentDetailsResponse getConsentDetails() throws SessionException {
        return apiClient.getConsentDetails();
    }
}
