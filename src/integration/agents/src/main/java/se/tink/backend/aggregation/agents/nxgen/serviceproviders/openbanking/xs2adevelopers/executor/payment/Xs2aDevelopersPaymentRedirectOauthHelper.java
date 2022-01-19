package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.TokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class Xs2aDevelopersPaymentRedirectOauthHelper implements OAuth2Authenticator {
    private final Xs2aDevelopersApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final Xs2aDevelopersProviderConfiguration configuration;

    @Override
    public URL buildAuthorizeUrl(String state) {
        String scaUrl = retrieveScaUrl();
        return apiClient.buildAuthorizeUrl(
                state, "PIS:" + persistentStorage.get(StorageKeys.PAYMENT_ID), scaUrl);
    }

    private String retrieveScaUrl() {
        String scaOAuthSourceUrl = getScaOAuthLinkFromStorage();
        if (isWellKnownURI(scaOAuthSourceUrl)) {
            return apiClient.getAuthorizationEndpointFromWellKnownURI(scaOAuthSourceUrl);
        }
        return scaOAuthSourceUrl;
    }

    private boolean isWellKnownURI(String uri) {
        return uri.contains("/.well-known/");
    }

    private String getScaOAuthLinkFromStorage() {
        return sessionStorage
                .get(StorageKeys.SCA_OAUTH_LINK, String.class)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        TokenForm tokenForm =
                TokenForm.builder()
                        .clientId(configuration.getClientId())
                        .code(code)
                        .codeVerifier(persistentStorage.get(StorageKeys.CODE_VERIFIER))
                        .grantType(FormValues.AUTHORIZATION_CODE)
                        .redirectUri(configuration.getRedirectUrl())
                        .validRequest(true)
                        .build();

        return apiClient.getToken(tokenForm).toTinkToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) {
        TokenForm refreshTokenForm =
                TokenForm.builder()
                        .clientId(configuration.getClientId())
                        .grantType(FormValues.REFRESH_TOKEN)
                        .refreshToken(refreshToken)
                        .build();

        return apiClient.getToken(refreshTokenForm).toTinkToken();
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(StorageKeys.PIS_TOKEN, accessToken);
    }
}
