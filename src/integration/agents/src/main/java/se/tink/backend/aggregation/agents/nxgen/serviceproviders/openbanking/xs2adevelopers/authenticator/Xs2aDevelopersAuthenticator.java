package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator;

import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.TokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenAccessor;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class Xs2aDevelopersAuthenticator implements OAuth2Authenticator, OAuth2TokenAccessor {

    protected final Xs2aDevelopersApiClient apiClient;
    protected final PersistentStorage persistentStorage;
    private final Xs2aDevelopersProviderConfiguration configuration;
    protected final LocalDateTimeSource localDateTimeSource;
    private final Credentials credentials;

    @Override
    public URL buildAuthorizeUrl(String state) {
        ConsentResponse consentResponse = requestForConsent();
        persistentStorage.put(StorageKeys.CONSENT_ID, consentResponse.getConsentId());
        String scaUrl = retrieveScaUrl(consentResponse);
        return apiClient.buildAuthorizeUrl(
                state, QueryValues.SCOPE + consentResponse.getConsentId(), scaUrl);
    }

    private ConsentResponse requestForConsent() {
        AccessEntity accessEntity = getAccessEntity();
        ConsentRequest consentRequest =
                new ConsentRequest(
                        accessEntity,
                        FormValues.FALSE,
                        FormValues.FREQUENCY_PER_DAY,
                        FormValues.TRUE,
                        localDateTimeSource.now().plusDays(89).format(DateTimeFormatter.ISO_DATE));
        return apiClient.createConsent(consentRequest);
    }

    private String retrieveScaUrl(ConsentResponse consentResponse) {
        String scaOAuthSourceUrl = consentResponse.getLinks().getScaOAuth();
        if (isWellKnownURI(scaOAuthSourceUrl)) {
            return apiClient.getAuthorizationEndpointFromWellKnownURI(scaOAuthSourceUrl);
        }
        return scaOAuthSourceUrl;
    }

    private boolean isWellKnownURI(String uri) {
        return uri.contains("/.well-known/");
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        TokenForm tokenForm =
                TokenForm.builder()
                        .setClientId(configuration.getClientId())
                        .setCode(code)
                        .setCodeVerifier(persistentStorage.get(StorageKeys.CODE_VERIFIER))
                        .setGrantType(FormValues.AUTHORIZATION_CODE)
                        .setRedirectUri(configuration.getRedirectUrl())
                        .setValidRequest(true)
                        .build();

        return apiClient.getToken(tokenForm).toTinkToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) {
        TokenForm refreshTokenForm =
                TokenForm.builder()
                        .setClientId(configuration.getClientId())
                        .setGrantType(FormValues.REFRESH_TOKEN)
                        .setRefreshToken(refreshToken)
                        .build();

        return apiClient.getToken(refreshTokenForm).toTinkToken();
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }

    protected AccessEntity getAccessEntity() {
        return new AccessEntity(FormValues.ALL_ACCOUNTS);
    }

    protected void storeConsentDetails() {
        ConsentDetailsResponse consentDetailsResponse = apiClient.getConsentDetails();
        credentials.setSessionExpiryDate(consentDetailsResponse.getValidUntil());
    }

    protected boolean isPersistedConsentValid() {
        ConsentStatusResponse consentStatusResponse = apiClient.getConsentStatus();
        return consentStatusResponse != null && consentStatusResponse.isValid();
    }

    @Override
    public void invalidate() {
        persistentStorage.clear();
    }

    @Override
    public OAuth2Token getAccessToken() {
        return persistentStorage
                .get(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }
}
