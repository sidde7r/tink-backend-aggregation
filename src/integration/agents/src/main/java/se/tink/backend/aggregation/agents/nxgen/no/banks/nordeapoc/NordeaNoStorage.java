package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class NordeaNoStorage {

    private static final String OAUTH_TOKEN = "token";
    private static final String DEVICE_ID = "deviceId";

    private final PersistentStorage persistentStorage;

    public void storeDeviceId(String deviceId) {
        persistentStorage.put(DEVICE_ID, deviceId);
    }

    public String retrieveDeviceId() {
        return persistentStorage.get(DEVICE_ID);
    }

    public void storeOauthToken(OAuth2Token oauthToken) {
        this.persistentStorage.put(OAUTH_TOKEN, oauthToken);
    }

    public Optional<OAuth2Token> retrieveOauthToken() {
        return persistentStorage.get(OAUTH_TOKEN, OAuth2Token.class);
    }
}
