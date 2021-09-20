package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class ArgentaStorage {

    private static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN;
    private static final String CODE_VERIFIER = "CODE_VERIFIER";
    private static final String CONSENT_ID = "CONSENT_ID";

    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    public void storeConsentId(String consentId) {
        persistentStorage.put(CONSENT_ID, consentId);
    }

    public void storeAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(OAUTH_TOKEN, accessToken);
    }

    void storeSessionCodeVerifier(String codeVerifier) {
        sessionStorage.put(CODE_VERIFIER, codeVerifier);
    }

    String getConsentId() {
        return persistentStorage.get(CONSENT_ID);
    }

    OAuth2Token getTokenFromStorageOrThrow() {
        return persistentStorage
                .get(OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    String getSessionCodeVerifier() {
        return sessionStorage.get(CODE_VERIFIER);
    }
}
