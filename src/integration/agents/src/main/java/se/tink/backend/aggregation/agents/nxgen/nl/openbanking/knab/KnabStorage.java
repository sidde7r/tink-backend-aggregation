package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class KnabStorage {

    private static final String CONSENT_ID = "consent_id";

    private final PersistentStorage persistentStorage;

    public Optional<String> findConsentId() {
        return persistentStorage.getOptional(CONSENT_ID);
    }

    public Optional<OAuth2Token> findBearerToken() {
        return persistentStorage.get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class);
    }

    public void persistConsentId(String consentId) {
        persistentStorage.put(CONSENT_ID, consentId);
    }

    public void persistBearerToken(OAuth2Token token) {
        persistentStorage.put(PersistentStorageKeys.OAUTH_2_TOKEN, token);
    }

    public void invalidatePersistedBearerToken() {
        if (findBearerToken().isPresent()) {
            persistentStorage.remove(PersistentStorageKeys.OAUTH_2_TOKEN);
        }
    }
}
