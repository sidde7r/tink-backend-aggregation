package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.authenticator;

import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.SebApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SebAuthenticator implements OAuth2Authenticator {
    private final SebApiClient client;
    private final SessionStorage sessionStorage;

    public SebAuthenticator(SebApiClient client, SessionStorage sessionStorage) {
        this.client = client;
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
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        OAuth2Token token = this.client.refreshToken(refreshToken);
        client.setTokenToSession(token);
        return token;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        client.setTokenToSession(accessToken);
    }
}
