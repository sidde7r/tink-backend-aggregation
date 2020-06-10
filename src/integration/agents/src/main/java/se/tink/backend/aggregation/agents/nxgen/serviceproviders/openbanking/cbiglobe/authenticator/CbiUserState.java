package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Date;
import java.util.List;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ScaMethodEntity;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CbiUserState {

    private static final String CBI_MANUAL_AUTHENTICATION_IN_PROGRESS =
            "cbi_manual_authentication_in_progress";
    private static final String CHOSEN_AUTHENTICATION_METHOD_ID = "chosen_authentication_method_id";
    private static final String SCA_METHODS = "sca_methods";

    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final Credentials credentials;

    public CbiUserState(
            final PersistentStorage persistentStorage,
            final SessionStorage sessionStorage,
            final Credentials credentials) {
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
    }

    public String getConsentId() {
        return persistentStorage
                .get(StorageKeys.CONSENT_ID, String.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    void startManualAuthenticationStep(String consentId) {
        persistentStorage.put(StorageKeys.CONSENT_ID, consentId);
        persistentStorage.put(CBI_MANUAL_AUTHENTICATION_IN_PROGRESS, true);
    }

    boolean isManualAuthenticationInProgress() {
        return persistentStorage
                .get(CBI_MANUAL_AUTHENTICATION_IN_PROGRESS, Boolean.class)
                .orElse(false);
    }

    void resetAuthenticationState() {
        persistentStorage.remove(StorageKeys.CONSENT_ID);
        persistentStorage.remove(StorageKeys.ACCOUNTS);
        finishManualAuthenticationStep();
    }

    void finishManualAuthenticationStep() {
        persistentStorage.put(CBI_MANUAL_AUTHENTICATION_IN_PROGRESS, false);
    }

    public void saveChosenAuthenticationMethod(String authenticationMethodId) {
        persistentStorage.put(CHOSEN_AUTHENTICATION_METHOD_ID, authenticationMethodId);
    }

    String getChosenAuthenticationMethodId() {
        return persistentStorage
                .get(CHOSEN_AUTHENTICATION_METHOD_ID, String.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find authentication method"));
    }

    void saveToken(OAuth2Token token) {
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, token);
    }

    public void saveScaMethods(List<ScaMethodEntity> scaMethods) {
        sessionStorage.put(SCA_METHODS, scaMethods);
    }

    public List<ScaMethodEntity> getScaMethods() {
        return sessionStorage
                .get(SCA_METHODS, new TypeReference<List<ScaMethodEntity>>() {})
                .orElseThrow(() -> new IllegalStateException("Cannot find sca methods"));
    }

    public void storeConsentExpiryDateInCredentials(Date expiryDate) {
        credentials.setSessionExpiryDate(expiryDate);
    }
}
