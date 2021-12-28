package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageValues.DECOUPLED_APPROACH;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.TokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessType;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenAccessor;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
@Slf4j
public class Xs2aDevelopersAuthenticatorHelper implements OAuth2Authenticator, OAuth2TokenAccessor {

    protected final Xs2aDevelopersApiClient apiClient;
    protected final PersistentStorage persistentStorage;
    protected final SessionStorage sessionStorage;
    private final Xs2aDevelopersProviderConfiguration configuration;
    protected final LocalDateTimeSource localDateTimeSource;
    private final Credentials credentials;

    @Override
    public URL buildAuthorizeUrl(String state) {
        String scaUrl = retrieveScaUrl();
        String consentId = getConsentIdFromStorage();
        return apiClient.buildAuthorizeUrl(state, QueryValues.SCOPE + consentId, scaUrl);
    }

    public void requestForConsent() {
        String psuId = credentials.getField(Key.USERNAME);
        AccessEntity accessEntity = getAccessEntity();
        ConsentRequest consentRequest =
                ConsentRequest.buildTypicalRecurring(accessEntity, localDateTimeSource);
        HttpResponse response = apiClient.createConsent(consentRequest, psuId);
        storeConsentValues(response);
    }

    private void storeConsentValues(HttpResponse response) {
        ConsentResponse consentResponse = response.getBody(ConsentResponse.class);
        List<String> scaApproachHeadersList = response.getHeaders().get("ASPSP-SCA-Approach");
        if (scaApproachHeadersList != null) {
            String scaApproach = scaApproachHeadersList.get(0);
            persistentStorage.put(StorageKeys.SCA_APPROACH, scaApproach);
            log.info("SCA approach - " + scaApproach);
        }
        persistentStorage.put(StorageKeys.CONSENT_ID, consentResponse.getConsentId());
        sessionStorage.put(StorageKeys.SCA_OAUTH_LINK, consentResponse.getLinks().getScaOAuth());
    }

    private String retrieveScaUrl() {
        String scaOAuthSourceUrl = getScaOAuthLinkFromStorage();
        if (isWellKnownURI(scaOAuthSourceUrl)) {
            return apiClient.getAuthorizationEndpointFromWellKnownURI(scaOAuthSourceUrl);
        }
        return scaOAuthSourceUrl;
    }

    private String getConsentIdFromStorage() {
        return persistentStorage
                .get(StorageKeys.CONSENT_ID, String.class)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }

    private String getScaOAuthLinkFromStorage() {
        return sessionStorage
                .get(StorageKeys.SCA_OAUTH_LINK, String.class)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }

    private boolean isWellKnownURI(String uri) {
        return uri.contains("/.well-known/");
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
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }

    protected AccessEntity getAccessEntity() {
        return AccessEntity.builder().allPsd2(AccessType.ALL_ACCOUNTS).build();
    }

    protected void storeConsentDetails() {
        ConsentDetailsResponse consentDetailsResponse = apiClient.fetchConsentDetails();
        credentials.setSessionExpiryDate(consentDetailsResponse.getValidUntil());
    }

    public ConsentStatusResponse fetchConsentStatus() {
        return apiClient.fetchConsentStatus();
    }

    void clearPersistentStorage() {
        persistentStorage.clear();
    }

    @Override
    public void invalidate() {
        clearPersistentStorage();
    }

    @Override
    public OAuth2Token getAccessToken() {
        return persistentStorage
                .get(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }

    boolean isDecoupledAuthenticationPossible() {
        String scaApproach = persistentStorage.get(StorageKeys.SCA_APPROACH);
        return DECOUPLED_APPROACH.equals(scaApproach);
    }
}
