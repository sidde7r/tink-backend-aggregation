package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator;

import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class IngAuthenticator implements OAuth2Authenticator {

    private final IngApiClient client;
    private final SessionStorage sessionStorage;

    public IngAuthenticator(IngApiClient apiClient, SessionStorage sessionStorage) {
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
