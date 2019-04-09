package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.URLs;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc.ExchangeAuthorizationCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.configuration.RabobankConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RabobankAuthenticator implements OAuth2Authenticator {

    private final RabobankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final RabobankConfiguration configuration;

    public RabobankAuthenticator(
            final RabobankApiClient apiClient,
            final PersistentStorage persistentStorage,
            final RabobankConfiguration configuration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
    }

    private RabobankConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public URL buildAuthorizeUrl(final String state) {
        final String redirectUri = getConfiguration().getRedirectUrl();
        final String clientId = getConfiguration().getClientId();

        return URLs.AUTHORIZE_RABOBANK
                .queryParam(QueryParams.RESPONSE_TYPE, QueryValues.CODE)
                .queryParam(QueryParams.REDIRECT_URI, redirectUri)
                .queryParam(QueryParams.CLIENT_ID, clientId)
                .queryParam(QueryParams.SCOPE, QueryValues.SCOPES)
                .queryParam(QueryParams.STATE, state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(final String code) {
        final String redirectUri = getConfiguration().getRedirectUrl();
        final ExchangeAuthorizationCodeRequest request = new ExchangeAuthorizationCodeRequest();

        request.put(QueryParams.GRANT_TYPE, QueryValues.AUTHORIZATION_CODE);
        request.put(QueryParams.CODE, code);
        request.put(QueryParams.REDIRECT_URI, redirectUri);

        return apiClient.exchangeAuthorizationCode(request).toOauthToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(final String refreshToken)
            throws SessionException, BankServiceException {
        final String redirectUri = getConfiguration().getRedirectUrl();
        final RefreshTokenRequest request = new RefreshTokenRequest();

        request.put(QueryParams.GRANT_TYPE, QueryValues.AUTHORIZATION_CODE);
        request.put(QueryParams.REDIRECT_URI, redirectUri);
        request.put(QueryParams.REFRESH_TOKEN, refreshToken);

        try {
            return apiClient.refreshAccessToken(request).toOauthToken();
        } catch (final HttpResponseException exception) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public void useAccessToken(final OAuth2Token accessToken) {
        persistentStorage.put(StorageKey.OAUTH_TOKEN, accessToken);
    }
}
