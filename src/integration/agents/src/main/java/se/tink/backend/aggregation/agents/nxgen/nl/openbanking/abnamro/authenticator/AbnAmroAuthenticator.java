package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.AbnAmroApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.AbnAmroConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.AbnAmroConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator.rpc.ExchangeAuthorizationCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.configuration.AbnAmroConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class AbnAmroAuthenticator implements OAuth2Authenticator {

    private final AbnAmroApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final AbnAmroConfiguration configuration;
    private final String redirectUrl;

    public AbnAmroAuthenticator(
            AbnAmroApiClient apiClient,
            PersistentStorage persistentStorage,
            AgentConfiguration<AbnAmroConfiguration> agentConfiguration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    @Override
    public URL buildAuthorizeUrl(final String state) {
        final String clientId = getConfiguration().getClientId();

        return AbnAmroConstants.URLs.AUTHORIZE_ABNAMRO
                .queryParam(AbnAmroConstants.QueryParams.SCOPE, AbnAmroConstants.QueryValues.SCOPES)
                .queryParam(AbnAmroConstants.QueryParams.CLIENT_ID, clientId)
                .queryParam(
                        AbnAmroConstants.QueryParams.RESPONSE_TYPE,
                        AbnAmroConstants.QueryValues.CODE)
                .queryParam(AbnAmroConstants.QueryParams.FLOW, AbnAmroConstants.QueryValues.CODE)
                .queryParam(AbnAmroConstants.QueryParams.REDIRECT_URI, redirectUrl)
                .queryParam(AbnAmroConstants.QueryParams.BANK, AbnAmroConstants.QueryValues.NLAA01)
                .queryParam(AbnAmroConstants.QueryParams.STATE, state);
    }

    private AbnAmroConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(final String code) throws BankServiceException {

        final String clientId = getConfiguration().getClientId();
        final ExchangeAuthorizationCodeRequest request = new ExchangeAuthorizationCodeRequest();

        request.put(
                AbnAmroConstants.QueryParams.GRANT_TYPE,
                AbnAmroConstants.QueryValues.AUTHORIZATION_CODE);
        request.put(AbnAmroConstants.QueryParams.CLIENT_ID, clientId);
        request.put(AbnAmroConstants.QueryParams.CODE, code);
        request.put(AbnAmroConstants.QueryParams.REDIRECT_URI, redirectUrl);

        return apiClient.exchangeAuthorizationCode(request).toOauthToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        final String clientId = getConfiguration().getClientId();
        final RefreshTokenRequest request = new RefreshTokenRequest();

        request.put(
                AbnAmroConstants.QueryParams.GRANT_TYPE,
                AbnAmroConstants.QueryValues.REFRESH_TOKEN);
        request.put(AbnAmroConstants.QueryParams.CLIENT_ID, clientId);
        request.put(AbnAmroConstants.QueryParams.REFRESH_TOKEN, refreshToken);

        try {
            return apiClient.refreshAccessToken(request).toOauthToken();
        } catch (final HttpResponseException exception) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public void useAccessToken(final OAuth2Token accessToken) {
        persistentStorage.put(AbnAmroConstants.StorageKey.OAUTH_TOKEN, accessToken);
    }

    void checkIfConsentValidOrThrowException() {
        ConsentResponse consentResponse = apiClient.consentRequest();
        if (!consentResponse.isValid()) {
            throw SessionError.CONSENT_EXPIRED.exception();
        }
    }
}
