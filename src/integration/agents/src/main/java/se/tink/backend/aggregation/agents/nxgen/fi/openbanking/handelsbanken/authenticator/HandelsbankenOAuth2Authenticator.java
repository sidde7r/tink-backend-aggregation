package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Scope;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.AuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.RedirectResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.configuration.HandelsbankenBaseConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class HandelsbankenOAuth2Authenticator implements OAuth2Authenticator {

    private static final String CONSENT_ID_KEY = "CONSENT_ID";

    private final HandelsbankenBaseApiClient apiClient;
    private final HandelsbankenBaseConfiguration configuration;
    private final String redirectUrl;
    private final PersistentStorage persistentStorage;

    public HandelsbankenOAuth2Authenticator(
            HandelsbankenBaseApiClient apiClient,
            AgentConfiguration<HandelsbankenBaseConfiguration> agentConfiguration,
            PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.persistentStorage = persistentStorage;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        TokenResponse tokenResponse =
                apiClient.requestClientCredentialGrantTokenWithScope(Scope.AIS);
        AuthorizationResponse consent = apiClient.initiateConsent(tokenResponse.getAccessToken());

        persistentStorage.put(CONSENT_ID_KEY, consent.getConsentId());

        return consent.getScaMethods().stream()
                .filter(scaMethod -> "REDIRECT".equals(scaMethod.getScaMethodType()))
                .findAny()
                .map(scaMethod -> scaMethod.getLinksEntity().getAuthorization().get(0).getHref())
                .map(
                        baseAuthUrl ->
                                decorateBaseAuthorizeUrl(
                                        baseAuthUrl, consent.getConsentId(), state))
                .orElseThrow(
                        () ->
                                new SessionException(
                                        SessionError.CONSENT_INVALID,
                                        "Bank did not provide redirect link for the given consent"));
    }

    private URL decorateBaseAuthorizeUrl(String baseAuthorizeUrl, String consentId, String state) {
        return new URL(baseAuthorizeUrl)
                .queryParam("response_type", "code")
                .queryParam("scope", "AIS:" + consentId)
                .queryParam("client_id", configuration.getClientId())
                .queryParam("state", state)
                .queryParam("redirect_uri", redirectUrl);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code)
            throws BankServiceException, AuthenticationException {

        RedirectResponse token =
                apiClient.exchangeAuthorizationCode(persistentStorage.get(CONSENT_ID_KEY), code);

        return OAuth2Token.createBearer(
                token.getAccessToken(), token.getRefreshToken(), token.getExpiresIn());
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {

        TokenResponse newAccessToken = apiClient.getRefreshToken(refreshToken);
        return toTinkToken(newAccessToken);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(PersistentStorageKeys.OAUTH_2_TOKEN, accessToken);
    }

    private OAuth2Token toTinkToken(TokenResponse token) {
        return OAuth2Token.createBearer(
                token.getAccessToken(), token.getAccessToken(), token.getExpiresIn());
    }
}
