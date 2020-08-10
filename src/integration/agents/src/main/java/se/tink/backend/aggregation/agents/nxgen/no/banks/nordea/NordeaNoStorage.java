package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea;

import java.util.Optional;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@AllArgsConstructor
public class NordeaNoStorage {

    private static final String OAUTH_TOKEN = "token";
    private static final String NORDEA_SESSION_ID = "sessionId";
    private static final String OIDC_SESSION_ID = "oidcSessionId";
    private static final String CODE_VERIFIER = "codeVerifier";
    private static final String DEVICE_ID = "deviceId";

    private PersistentStorage persistentStorage;
    private SessionStorage sessionStorage;

    public String retrieveSessionId() {
        return sessionStorage.get(NORDEA_SESSION_ID);
    }

    public void storeSessionId(String sessionId) {
        sessionStorage.put(NORDEA_SESSION_ID, sessionId);
    }

    public void storeCodeVerifier(String codeVerifier) {
        sessionStorage.put(CODE_VERIFIER, codeVerifier);
    }

    public String retrieveCodeVerifier() {
        return sessionStorage.get(CODE_VERIFIER);
    }

    public void storeOidcSessionId(String oidcSessionId) {
        sessionStorage.put(OIDC_SESSION_ID, oidcSessionId);
    }

    public String retrievOidcSessionId() {
        return sessionStorage.get(OIDC_SESSION_ID);
    }

    public void storeDeviceId(String deviceId) {
        persistentStorage.put(DEVICE_ID, deviceId);
    }

    public String retrieveDeviceId() {
        return persistentStorage.get(DEVICE_ID);
    }

    public void storeOauthToken(OAuth2Token oauthToken) {
        this.persistentStorage.put(OAUTH_TOKEN, oauthToken);
        this.sessionStorage.put(OAUTH_TOKEN, oauthToken);
    }

    public Optional<OAuth2Token> retrieveOauthToken() {
        return persistentStorage.get(OAUTH_TOKEN, OAuth2Token.class);
    }
}
