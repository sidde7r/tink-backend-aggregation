package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient;

import java.util.Optional;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@AllArgsConstructor
public class CmcicRepository {

    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    void storePispToken(OAuth2Token token) {
        sessionStorage.put(StorageKeys.PISP_TOKEN, token);
    }

    Optional<OAuth2Token> findPispToken() {
        return sessionStorage.get(StorageKeys.PISP_TOKEN, OAuth2Token.class);
    }

    OAuth2Token getAispToken() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    void storeCodeVerifier(String codeVerifier) {
        sessionStorage.put(StorageKeys.CODE_VERIFIER, codeVerifier);
    }

    String getCodeVerifier() {
        return sessionStorage.get(StorageKeys.CODE_VERIFIER);
    }
}
