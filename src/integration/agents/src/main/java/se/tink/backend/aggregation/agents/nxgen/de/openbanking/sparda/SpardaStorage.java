package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class SpardaStorage {

    private static final String CONSENT_ID = "consentId";
    private static final String CODE_VERIFIER = "codeVerifier";

    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    public void saveConsentId(String consentId) {
        persistentStorage.put(CONSENT_ID, consentId);
    }

    public String getConsentId() {
        return persistentStorage.get(CONSENT_ID);
    }

    public void saveToken(OAuth2Token accessToken) {
        persistentStorage.put(OAUTH_2_TOKEN, accessToken);
    }

    public OAuth2Token getToken() {
        return persistentStorage
                .get(OAUTH_2_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () ->
                                SessionError.SESSION_EXPIRED.exception(
                                        "Access token not found in storage when expected!"));
    }

    public void saveCodeVerifier(String codeVerifier) {
        sessionStorage.put(CODE_VERIFIER, codeVerifier);
    }

    public String getCodeVerifier() {
        return sessionStorage.get(CODE_VERIFIER);
    }
}
