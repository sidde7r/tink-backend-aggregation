package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class OAuth2TokenStorageDefaultImpl implements OAuth2TokenStorage {

    private static final String STORAGE_KEY = "OAUTH2_TOKEN";
    private PersistentStorage persistentStorage;

    public OAuth2TokenStorageDefaultImpl(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    @Override
    public Optional<OAuth2Token> fetchToken() {
        return persistentStorage.get(STORAGE_KEY, OAuth2Token.class);
    }

    @Override
    public void storeToken(OAuth2Token token) {
        persistentStorage.put(STORAGE_KEY, token);
    }
}
