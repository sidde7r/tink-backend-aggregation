package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator;

import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class IngBaseAuthenticator implements OAuth2Authenticator {

    private final IngBaseApiClient client;
    private final SessionStorage sessionStorage;

    public IngBaseAuthenticator(IngBaseApiClient apiClient, SessionStorage sessionStorage) {
        this.client = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return client.getAuthorizeUrl(state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        return client.getToken(code);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws BankServiceException {
        final OAuth2Token token = this.client.refreshToken(refreshToken);
        client.setTokenToSession(token);
        return token;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        client.setTokenToSession(accessToken);
    }
}
