package se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class MediolanumStorage {

    private static final String CONSENT_ID = "consentId";
    private final PersistentStorage persistentStorage;

    public String getConsentId() {
        return persistentStorage.get(CONSENT_ID);
    }

    public void saveConsentId(String consentId) {
        persistentStorage.put(CONSENT_ID, consentId);
    }

    public void saveToken(OAuth2Token accessToken) {
        persistentStorage.put(OAUTH_2_TOKEN, accessToken);
    }

    public Optional<OAuth2Token> getToken() {
        return persistentStorage.get(OAUTH_2_TOKEN, OAuth2Token.class);
    }
}
