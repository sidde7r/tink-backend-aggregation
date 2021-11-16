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

    public String getPaymentId() {
        return sessionStorage.get(StorageKeys.PAYMENT_ID);
    }

    public void storeAuthorizationCode(String authorizationCode) {
        sessionStorage.put(StorageKeys.AUTHORIZATION_CODE, authorizationCode);
    }

    public String getAuthorizationCode() {
        return sessionStorage.get(StorageKeys.AUTHORIZATION_CODE);
    }

    public String getState() {
        return sessionStorage.get(StorageKeys.STATE);
    }

    public void storeState(String state) {
        sessionStorage.put(StorageKeys.STATE, state);
    }

    public void storeAuthorizationUrl(String authorizeUrl) {
        sessionStorage.put(StorageKeys.AUTH_URL, authorizeUrl);
    }

    public String getAuthorizationUrl() {
        return sessionStorage.get(StorageKeys.AUTH_URL);
    }

    public void storePispToken(OAuth2Token token) {
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

    void storePaymentId(String paymentId) {
        sessionStorage.put(StorageKeys.PAYMENT_ID, paymentId);
    }
}
