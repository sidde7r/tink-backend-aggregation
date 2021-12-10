package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class KnabAuthenticator implements OAuth2Authenticator {

    private final StrongAuthenticationState strongAuthenticationState;

    private final KnabApiClient apiClient;

    private final KnabStorage storage;

    @Override
    public URL buildAuthorizeUrl(String state) {
        OAuth2Token applicationAccessToken = apiClient.applicationAccessToken();
        String anonymousConsentId = apiClient.anonymousConsent(applicationAccessToken);

        storage.persistConsentId(anonymousConsentId);

        return apiClient.anonymousConsentApprovalUrl(consentScope(anonymousConsentId), state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        return apiClient.accessToken(code, strongAuthenticationState.getState());
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) {
        OAuth2Token accessToken = apiClient.refreshToken(refreshToken);

        storage.persistBearerToken(accessToken);

        return accessToken;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        storage.findConsentId()
                .filter(consentId -> apiClient.consentStatus(consentId, accessToken))
                .ifPresent(ignored -> storage.persistBearerToken(accessToken));
    }

    private String consentScope(String consentId) {
        return String.format("psd2 offline_access AIS:%s", consentId);
    }
}
