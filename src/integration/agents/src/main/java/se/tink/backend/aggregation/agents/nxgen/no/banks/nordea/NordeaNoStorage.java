package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class NordeaNoStorage {

    private static final String OAUTH_TOKEN = "token";
    private static final String CODE_VERIFIER = "codeVerifier";

    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    public void storeCodeVerifier(String codeVerifier) {
        sessionStorage.put(CODE_VERIFIER, codeVerifier);
    }

    public String retrieveCodeVerifier() {
        return sessionStorage.get(CODE_VERIFIER);
    }

    public void storeOauthToken(OAuth2Token oauthToken) {
        this.persistentStorage.put(OAUTH_TOKEN, oauthToken);
        this.sessionStorage.put(OAUTH_TOKEN, oauthToken);
    }

    public Optional<OAuth2Token> retrieveOauthToken() {
        return persistentStorage.get(OAUTH_TOKEN, OAuth2Token.class);
    }
}
