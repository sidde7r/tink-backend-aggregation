package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclTokenApiClient;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenBase;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@AllArgsConstructor
public class TokenFetcher {

    private static final String TOKEN = "pis_token";
    private final LclTokenApiClient tokenApiClient;
    private final SessionStorage sessionStorage;

    public void fetchToken() {
        if (!isTokenValid()) {
            getAndSaveToken();
        }
    }

    public OAuth2Token reuseTokenOrRefetch() {
        return refetchIfExpired(getTokenFromStorage());
    }

    private boolean isTokenValid() {
        return sessionStorage
                .get(TOKEN, OAuth2Token.class)
                .map(OAuth2TokenBase::isValid)
                .orElse(false);
    }

    private void getAndSaveToken() {
        OAuth2Token token = getToken();
        sessionStorage.put(TOKEN, token);
    }

    private OAuth2Token getToken() {
        return tokenApiClient.getPispToken().toOauthToken();
    }

    private OAuth2Token refetchIfExpired(OAuth2Token oAuth2Token) {
        if (!oAuth2Token.canUseAccessToken()) {
            return getToken();
        } else {
            return oAuth2Token;
        }
    }

    private OAuth2Token getTokenFromStorage() {
        return sessionStorage
                .get(TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalArgumentException("Access token not found in storage."));
    }
}
