package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.RaiffeisenApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;

public class RaiffeisenOAuth2Authenticator implements OAuth2Authenticator {

    private final RaiffeisenApiClient client;

    public RaiffeisenOAuth2Authenticator(RaiffeisenApiClient client) {
        this.client = client;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return client.getAuthorizeUrl(state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        return this.client.getToken(code);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws SessionException {
        OAuth2Token token = client.refreshToken(refreshToken);
        client.setToken(token);
        return token;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        client.setToken(accessToken);
    }
}
