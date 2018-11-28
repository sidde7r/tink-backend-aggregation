package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.authenticator;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.MonzoApiClient;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.MonzoConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.authenticator.rpc.ExchangeRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class MonzoAuthenticator implements OAuth2Authenticator {

    private final MonzoApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public MonzoAuthenticator(MonzoApiClient client, PersistentStorage storage) {
        apiClient = client;
        persistentStorage = storage;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return new URL(MonzoConstants.URL.AUTH_MONZO_COM)
                .queryParam(MonzoConstants.RequestKey.CLIENT_ID, this.getClientId())
                .queryParam(MonzoConstants.RequestKey.REDIRECT_URI, this.getRedirectUrl())
                .queryParam(MonzoConstants.RequestKey.RESPONSE_TYPE, MonzoConstants.RequestValue.CODE)
                .queryParam(MonzoConstants.RequestKey.STATE, state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {

        ExchangeRequest request = new ExchangeRequest();
        request.put(MonzoConstants.RequestKey.GRANT_TYPE, MonzoConstants.RequestValue.AUTHORIZATION_CODE);
        request.put(MonzoConstants.RequestKey.CLIENT_ID, this.getClientId());
        request.put(MonzoConstants.RequestKey.CLIENT_SECRET, this.getClientSecret());
        request.put(MonzoConstants.RequestKey.REDIRECT_URI, this.getRedirectUrl());
        request.put(MonzoConstants.RequestKey.CODE, code);

        TokenResponse response = apiClient.exchangeAuthorizationCode(request);

        return this.convert(response);

    }

    private OAuth2Token convert(TokenResponse response) {
        return OAuth2Token.create(response.getTokenType(), response.getAccessToken(), response.getRefreshToken(),
                response.getExpiresIn());
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws SessionException {

        try {

            RefreshRequest request = new RefreshRequest();
            request.put(MonzoConstants.RequestKey.GRANT_TYPE, MonzoConstants.RequestValue.REFRESH_TOKEN);
            request.put(MonzoConstants.RequestKey.CLIENT_ID, this.getClientId());
            request.put(MonzoConstants.RequestKey.CLIENT_SECRET, this.getClientSecret());
            request.put(MonzoConstants.RequestKey.REFRESH_TOKEN, refreshToken);

            TokenResponse response = apiClient.refreshAccessToken(request);

            return this.convert(response);

        } catch (HttpResponseException x) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(MonzoConstants.StorageKey.OAUTH_TOKEN, accessToken);
    }

    private String getClientId() {

        String clientId = persistentStorage.get(MonzoConstants.StorageKey.CLIENT_ID);

        if (Strings.isNullOrEmpty(clientId)) {
            throw new IllegalStateException("CLIENT_ID cannot be null or empty!");
        }

        return clientId;
    }

    private String getClientSecret() {

        String clientSecret = persistentStorage.get(MonzoConstants.StorageKey.CLIENT_SECRET);

        if (Strings.isNullOrEmpty(clientSecret)) {
            throw new IllegalStateException("CLIENT_SECRET cannot be null or empty!");
        }

        return clientSecret;
    }

    private String getRedirectUrl() {

        String redirectUrl = persistentStorage.get(MonzoConstants.StorageKey.REDIRECT_URL);

        if (Strings.isNullOrEmpty(redirectUrl)) {
            throw new IllegalStateException("REDIRECT_URL cannot be null or empty!");
        }

        return redirectUrl;
    }

}
