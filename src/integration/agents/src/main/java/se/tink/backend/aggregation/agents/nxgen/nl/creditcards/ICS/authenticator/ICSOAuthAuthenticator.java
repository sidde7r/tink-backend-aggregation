package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator;

import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;

public class ICSOAuthAuthenticator implements OAuth2Authenticator {

    private final ICSApiClient client;

    public ICSOAuthAuthenticator(ICSApiClient client) {
        this.client = client;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return client.getAuthorizeUrl(state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        return client.fetchToken(code);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        return client.refreshToken(refreshToken);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        this.client.setToken(accessToken);
    }
}
