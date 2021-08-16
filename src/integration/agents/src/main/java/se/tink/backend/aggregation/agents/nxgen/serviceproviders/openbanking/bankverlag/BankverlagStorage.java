package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class BankverlagStorage {

    private static final String CONSENT_ID = "consentId";
    private static final String FIRST_FETCH_FLAG = "firstFetch";
    private static final String DONE = "done";
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private static final String AUTHENTICATION_METHOD = "authMethod";
    private static final String PUSH_OTP = "PUSH_OTP";

    public String getConsentId() {
        return persistentStorage.get(CONSENT_ID);
    }

    public void saveConsentId(String consentId) {
        persistentStorage.put(CONSENT_ID, consentId);
    }

    public boolean isFirstFetch() {
        return !DONE.equals(persistentStorage.get(FIRST_FETCH_FLAG));
    }

    public void markFirstFetchAsDone() {
        persistentStorage.put(FIRST_FETCH_FLAG, DONE);
    }

    public void savePushOtpFromHeader() {
        sessionStorage.put(AUTHENTICATION_METHOD, PUSH_OTP);
    }

    public String getPushOtpFromHeader() {
        return sessionStorage.get(AUTHENTICATION_METHOD);
    }
}
