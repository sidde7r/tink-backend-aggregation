package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.authenticator;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.MonzoApiClient;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.MonzoConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.MonzoConstants.RequestKey;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.MonzoConstants.RequestValue;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.MonzoConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.MonzoConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.authenticator.rpc.ExchangeRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.configuration.MonzoConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class MonzoAuthenticator implements OAuth2Authenticator {

    private final MonzoApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final MonzoConfiguration configuration;

    public MonzoAuthenticator(
            MonzoApiClient client, PersistentStorage storage, MonzoConfiguration configuration) {
        this.apiClient = client;
        this.persistentStorage = storage;
        this.configuration = configuration;
    }

    public MonzoConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        final String clientId = getConfiguration().getClientId();
        final String redirectUrl = getConfiguration().getRedirectUrl();

        return new URL(Urls.AUTH_MONZO_COM)
                .queryParam(RequestKey.CLIENT_ID, clientId)
                .queryParam(RequestKey.REDIRECT_URI, redirectUrl)
                .queryParam(RequestKey.RESPONSE_TYPE, RequestValue.CODE)
                .queryParam(RequestKey.STATE, state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();
        final String redirectUrl = getConfiguration().getRedirectUrl();

        final ExchangeRequest request = new ExchangeRequest();
        request.put(RequestKey.GRANT_TYPE, RequestValue.AUTHORIZATION_CODE);
        request.put(RequestKey.CLIENT_ID, clientId);
        request.put(RequestKey.CLIENT_SECRET, clientSecret);
        request.put(RequestKey.REDIRECT_URI, redirectUrl);
        request.put(RequestKey.CODE, code);

        return apiClient.exchangeAuthorizationCode(request).toTinkToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws SessionException {
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();

        final RefreshRequest request = new RefreshRequest();
        request.put(RequestKey.GRANT_TYPE, RequestValue.REFRESH_TOKEN);
        request.put(RequestKey.CLIENT_ID, clientId);
        request.put(RequestKey.CLIENT_SECRET, clientSecret);
        request.put(RequestKey.REFRESH_TOKEN, refreshToken);

        try {
            return apiClient.refreshAccessToken(request).toTinkToken();
        } catch (HttpResponseException x) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(StorageKey.OAUTH_TOKEN, accessToken);
    }
}
