package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator;

import com.google.api.client.util.Strings;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc.ExchangeAuthorizationCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RabobankAuthenticator implements OAuth2Authenticator {

    private final PersistentStorage persistentStorage;
    private final RabobankApiClient apiClient;

    public RabobankAuthenticator(final RabobankApiClient apiClient, final PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return new URL(RabobankConstants.URL.AUTHORIZE_RABOBANK)
                .queryParam(RabobankConstants.QueryParams.RESPONSE_TYPE, RabobankConstants.QueryValues.CODE)
                .queryParam(RabobankConstants.QueryParams.REDIRECT_URI, getRedirectUri())
                .queryParam(RabobankConstants.QueryParams.CLIENT_ID, getClientId())
                .queryParam(RabobankConstants.QueryParams.SCOPE, "ais.balances.read ais.transactions.read-90days ais.transactions.read-history")
                .queryParam(RabobankConstants.QueryParams.STATE, state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code){
        ExchangeAuthorizationCodeRequest request = new ExchangeAuthorizationCodeRequest();
        request.put(RabobankConstants.QueryParams.GRANT_TYPE, RabobankConstants.QueryValues.AUTHORIZATION_CODE);
        request.put(RabobankConstants.QueryParams.CODE, code);
        request.put(RabobankConstants.QueryParams.REDIRECT_URI, getRedirectUri());
        return apiClient.exchangeAuthorizationCode(request).toOauthToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws SessionException, BankServiceException {
        try {
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.put(RabobankConstants.QueryParams.GRANT_TYPE, RabobankConstants.QueryValues.AUTHORIZATION_CODE);
            request.put(RabobankConstants.QueryParams.REDIRECT_URI, getRedirectUri());
            request.put(RabobankConstants.QueryParams.REFRESH_TOKEN, refreshToken);

            return apiClient.refreshAccessToken(request).toOauthToken();
        } catch (HttpResponseException exception) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(RabobankConstants.StorageKey.OAUTH_TOKEN, accessToken);
    }

    private String getClientId() {
        final String clientId = persistentStorage.get(RabobankConstants.StorageKey.CLIENT_ID);
        if (Strings.isNullOrEmpty(clientId)) {
            throw new IllegalStateException("clientId is null or empty!");
        }

        return clientId;
    }

    private String getRedirectUri() {
        final String redirectUri = persistentStorage.get(RabobankConstants.StorageKey.REDIRECT_URL);
        if (Strings.isNullOrEmpty(redirectUri)) {
            throw new IllegalStateException("redirectUri is null or empty!");
        }

        return redirectUri;
    }
}
