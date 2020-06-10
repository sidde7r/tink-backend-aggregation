package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenStorage;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class OAuth2TokenStorage implements AccessTokenStorage<OAuth2Token> {

    private static final String OAUTH_2_TOKEN_KEY = "oauth2_access_token";

    private static final String TMP_TOKEN_STORAGE_KEY = "tmp_oauth2_token";

    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    @Override
    public Optional<OAuth2Token> getToken() {
        return persistentStorage.get(OAUTH_2_TOKEN_KEY, OAuth2Token.class);
    }

    @Override
    public Optional<OAuth2Token> getTokenFromSession() {
        return sessionStorage.get(TMP_TOKEN_STORAGE_KEY, OAuth2Token.class);
    }

    @Override
    public void storeToken(OAuth2Token oAuth2Token) {
        persistentStorage.put(OAUTH_2_TOKEN_KEY, oAuth2Token);
    }

    @Override
    public void storeTokenInSession(OAuth2Token oAuth2Token) {
        sessionStorage.put(TMP_TOKEN_STORAGE_KEY, oAuth2Token);
    }

    @Override
    public void rotateToken(OAuth2Token oAuth2Token) {
        persistentStorage.rotateStorageValue(OAUTH_2_TOKEN_KEY, oAuth2Token);
    }

    @Override
    public void clearToken() {
        persistentStorage.remove(OAUTH_2_TOKEN_KEY);
    }
}
