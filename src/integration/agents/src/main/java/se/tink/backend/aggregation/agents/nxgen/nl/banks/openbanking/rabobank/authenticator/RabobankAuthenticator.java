package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator;

import com.google.common.base.Strings;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.configuration.RabobankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.utils.RabobankUtils;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.CallbackParams;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class RabobankAuthenticator implements OAuth2Authenticator {

    private final RabobankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final RabobankConfiguration configuration;
    private final String redirectUrl;

    public RabobankAuthenticator(
            final RabobankApiClient apiClient,
            final PersistentStorage persistentStorage,
            final AgentConfiguration<RabobankConfiguration> agentConfiguration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    private RabobankConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    private String getRedirectUrl() {
        return Optional.ofNullable(redirectUrl)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public URL buildAuthorizeUrl(final String state) {
        log.info("[Rabobank] Started building authorize url");

        final String clientId = getConfiguration().getClientId();

        final Form params =
                Form.builder()
                        .encodeSpacesWithPercent()
                        .put(QueryParams.RESPONSE_TYPE, QueryValues.CODE)
                        .put(QueryParams.REDIRECT_URI, getRedirectUrl())
                        .put(QueryParams.CLIENT_ID, clientId)
                        .put(QueryParams.SCOPE, QueryValues.SCOPES)
                        .put(QueryParams.STATE, state)
                        .build();

        return configuration.getUrls().getAuthorizeUrl().concat("?" + params.serialize());
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(final String code) {

        final Form request =
                Form.builder()
                        .put(QueryParams.GRANT_TYPE, QueryValues.AUTHORIZATION_CODE)
                        .put(QueryParams.CODE, code)
                        .put(QueryParams.REDIRECT_URI, getRedirectUrl())
                        .build();

        final TokenResponse tokenResponse = apiClient.exchangeAuthorizationCode(request);
        persistentStorage.put(StorageKey.CONSENT_ID, tokenResponse.getConsentId());
        return tokenResponse.toOauthToken(persistentStorage);
    }

    @Override
    public OAuth2Token refreshAccessToken(final String refreshToken)
            throws SessionException, BankServiceException {

        apiClient.checkConsentStatus();

        final Form request =
                Form.builder()
                        .put(QueryParams.GRANT_TYPE, QueryValues.REFRESH_TOKEN)
                        .put(QueryParams.REDIRECT_URI, getRedirectUrl())
                        .put(QueryParams.REFRESH_TOKEN, refreshToken)
                        .build();

        try {
            OAuth2Token token =
                    apiClient.refreshAccessToken(request).toOauthToken(persistentStorage);
            // TODO: Remove log below after TC-4786 is done/closed
            log.info(
                    "Refreshed token to persist : {}",
                    Hash.sha256AsHex(token.getRefreshToken().get()));
            return token;
        } catch (final HttpResponseException exception) {
            // TODO: Remove log below after TC-4786 is done/closed
            if (exception.getResponse().getBody(String.class).contains("invalid_grant")) {
                final OAuth2Token oAuth2Token = RabobankUtils.getOauthToken(persistentStorage);
                final String refreshedTokenExpireDate =
                        persistentStorage.get(StorageKey.TOKEN_EXPIRY_DATE);
                log.info(
                        "Refresh token: {}, Is token expire? {}, Expiry date: {}",
                        Hash.sha256AsHex(oAuth2Token.getRefreshToken().get()),
                        oAuth2Token.hasRefreshExpire(),
                        refreshedTokenExpireDate);
            }
            throw SessionError.SESSION_EXPIRED.exception(exception);
        }
    }

    @Override
    public void handleSpecificCallbackDataError(Map<String, String> callbackData)
            throws AuthenticationException {
        String errorType = callbackData.getOrDefault(CallbackParams.ERROR, "");

        // From docs: If the user cancels before authorizing your application, then the response
        // from the Rabobank OAuth 2.0 server to your application's URL contains an error message
        // 'access_denied'.
        if (!Strings.isNullOrEmpty(errorType)
                && OAuth2Constants.ErrorType.ACCESS_DENIED.getValue().equalsIgnoreCase(errorType)) {
            throw ThirdPartyAppError.CANCELLED.exception();
        }
    }

    @Override
    public void useAccessToken(final OAuth2Token accessToken) {}

    void checkConsentStatus() {
        apiClient.checkConsentStatus();
    }
}
