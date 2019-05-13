package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.authenticator;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.AbnAmroApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.AbnAmroConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.AbnAmroConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.authenticator.rpc.ExchangeAuthorizationCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.configuration.AbnAmroConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class AbnAmroAuthenticator implements OAuth2Authenticator {

    private final AbnAmroApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final AbnAmroConfiguration configuration;

    public AbnAmroAuthenticator(
            AbnAmroApiClient apiClient,
            PersistentStorage persistentStorage,
            AbnAmroConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    @Override
    public URL buildAuthorizeUrl(final String state) {
        final String clientId = getConfiguration().getClientId();
        final String redirectUri = getConfiguration().getRedirectUrl();

        return AbnAmroConstants.URLs.AUTHORIZE_ABNAMRO
                .queryParam(AbnAmroConstants.QueryParams.SCOPE, AbnAmroConstants.QueryValues.SCOPES)
                .queryParam(AbnAmroConstants.QueryParams.CLIENT_ID, clientId)
                .queryParam(
                        AbnAmroConstants.QueryParams.RESPONSE_TYPE,
                        AbnAmroConstants.QueryValues.CODE)
                .queryParam(AbnAmroConstants.QueryParams.FLOW, AbnAmroConstants.QueryValues.CODE)
                .queryParam(AbnAmroConstants.QueryParams.REDIRECT_URI, redirectUri)
                .queryParam(AbnAmroConstants.QueryParams.BANK, AbnAmroConstants.QueryValues.NLAA01)
                .queryParam(AbnAmroConstants.QueryParams.STATE, state);
    }

    private AbnAmroConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(final String code) throws BankServiceException {

        final String redirectUri = getConfiguration().getRedirectUrl();
        final String clientId = getConfiguration().getClientId();
        final ExchangeAuthorizationCodeRequest request = new ExchangeAuthorizationCodeRequest();

        request.put(
                AbnAmroConstants.QueryParams.GRANT_TYPE,
                AbnAmroConstants.QueryValues.AUTHORIZATION_CODE);
        request.put(AbnAmroConstants.QueryParams.CLIENT_ID, clientId);
        request.put(AbnAmroConstants.QueryParams.CODE, code);
        request.put(AbnAmroConstants.QueryParams.REDIRECT_URI, redirectUri);

        OAuth2Token token = apiClient.exchangeAuthorizationCode(request).toOauthToken();

        if (!persistentStorage.containsKey(AbnAmroConstants.StorageKey.ACCOUNT_CONSENT_ID)) {
            ConsentResponse consent = apiClient.consentRequest(token);
            persistentStorage.put(AbnAmroConstants.StorageKey.ACCOUNT_CONSENT_ID, consent.getAccountId());
        }
        return token;
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        final String clientId = getConfiguration().getClientId();
        final RefreshTokenRequest request = new RefreshTokenRequest();

        request.put(
                AbnAmroConstants.QueryParams.GRANT_TYPE,
                AbnAmroConstants.QueryValues.AUTHORIZATION_CODE);
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
}
