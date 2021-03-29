package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SparkassenStorage {

    private static final String CONSENT_ID = "consentId";
    private static final String AUTHORIZATION_ID = "authorizationId";
    private static final String FIRST_FETCH_FLAG = "firstFetch";
    private static final String DONE = "done";
    private static final String CODE_VERIFIER = "codeVerifier";
    private static final String TOKEN_ENDPOINT = "tokenEndpoint";
    private final PersistentStorage persistentStorage;

    public SparkassenStorage(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public String getConsentId() {
        return persistentStorage.get(CONSENT_ID);
    }

    public void saveConsentId(String consentId) {
        persistentStorage.put(CONSENT_ID, consentId);
    }

    public String getAuthorizationId() {
        return persistentStorage.get(AUTHORIZATION_ID);
    }

    public void saveAuthorizationId(String authorizationId) {
        persistentStorage.put(AUTHORIZATION_ID, authorizationId);
    }

    public boolean isFirstFetch() {
        return !DONE.equals(persistentStorage.get(FIRST_FETCH_FLAG));
    }

    public void markFirstFetchAsDone() {
        persistentStorage.put(FIRST_FETCH_FLAG, DONE);
    }

    public void saveToken(OAuth2Token accessToken) {
        persistentStorage.put(OAUTH_2_TOKEN, accessToken);
    }

    public void saveCodeVerifier(String codeVerifier) {
        persistentStorage.put(CODE_VERIFIER, codeVerifier);
    }

    public void saveTokenEndpoint(String tokenEndpoint) {
        persistentStorage.put(TOKEN_ENDPOINT, tokenEndpoint);
    }

    public Optional<OAuth2Token> getToken() {
        return persistentStorage.get(OAUTH_2_TOKEN, OAuth2Token.class);
    }

    public String getCodeVerifier() {
        return persistentStorage.get(CODE_VERIFIER);
    }

    public String getTokenEndpoint() {
        return persistentStorage.get(TOKEN_ENDPOINT);
    }
}
