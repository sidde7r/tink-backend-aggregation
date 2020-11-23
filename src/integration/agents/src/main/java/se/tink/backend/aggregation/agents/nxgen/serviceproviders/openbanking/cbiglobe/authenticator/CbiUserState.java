package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import java.util.Date;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CbiUserState {

    private static final String CBI_MANUAL_AUTHENTICATION_IN_PROGRESS =
            "cbi_manual_authentication_in_progress";
    private static final String CHOSEN_AUTHENTICATION_METHOD_ID = "chosen_authentication_method_id";

    private final PersistentStorage persistentStorage;
    private final Credentials credentials;

    public CbiUserState(final PersistentStorage persistentStorage, final Credentials credentials) {
        this.persistentStorage = persistentStorage;
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

    public void finishManualAuthenticationStep() {
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

    public GetAccountsResponse getAccountsResponseFromStorage() {
        return SerializationUtils.deserializeFromString(
                persistentStorage.get(StorageKeys.ACCOUNTS), GetAccountsResponse.class);
    }

    public void persistAccounts(GetAccountsResponse getAccountsResponse) {
        persistentStorage.put(StorageKeys.ACCOUNTS, getAccountsResponse);
    }

    void saveToken(OAuth2Token token) {
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, token);
    }

    public void storeConsentExpiryDateInCredentials(Date expiryDate) {
        credentials.setSessionExpiryDate(expiryDate);
    }
}
