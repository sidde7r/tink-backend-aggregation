package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag;

import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BankverlagStorage {

    private static final String CONSENT_ID = "consentId";
    private static final String FIRST_FETCH_FLAG = "firstFetch";
    private static final String DONE = "done";
    private final PersistentStorage persistentStorage;

    public BankverlagStorage(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

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
}
