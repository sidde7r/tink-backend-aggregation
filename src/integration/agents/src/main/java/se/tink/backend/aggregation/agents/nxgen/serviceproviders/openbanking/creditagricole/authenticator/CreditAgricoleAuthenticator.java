package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth1.OAuth1Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth1Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CreditAgricoleAuthenticator implements OAuth1Authenticator {
    private final CreditAgricoleApiClient client;
    private final SessionStorage sessionStorage;

    public CreditAgricoleAuthenticator(
            CreditAgricoleApiClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public URL buildAuthorizeUrl(String oauthToken) {
        return client.getAuthorizeUrl(oauthToken);
    }

    @Override
    public OAuth1Token getRequestToken(String state) {
        return client.getRequestToken(state);
    }

    @Override
    public OAuth1Token getAccessToken(String oauthToken, String oauthVerifier)
            throws SessionException {
        return client.getAccessToken(oauthToken, oauthVerifier);
    }

    @Override
    public void useAccessToken(OAuth1Token temporaryToken) {
        client.setTokenToSession(temporaryToken);
    }
}
