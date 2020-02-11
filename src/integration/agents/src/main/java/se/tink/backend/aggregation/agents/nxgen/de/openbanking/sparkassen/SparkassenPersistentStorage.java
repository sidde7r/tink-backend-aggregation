package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen;

import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SparkassenPersistentStorage {

    private static final String CONSENT_ID = "consentId";
    private static final String AUTHORIZATION_ID = "authorizationId";
    private static final String FIRST_FETCH_FLAG = "firstFetch";
    private static final String DONE = "done";
    private final PersistentStorage persistentStorage;

    public SparkassenPersistentStorage(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public String getConsentId() {
        return persistentStorage.get(CONSENT_ID);
    }

    public void saveConsentId(String consentId) {
        persistentStorage.put(CONSENT_ID, consentId);
    }

    public String getAuthorizationId() {
        return persistentStorage.get(AUTHORIZATION_ID);
    }

    public void saveAuthorizationId(String authorizationId) {
        persistentStorage.put(AUTHORIZATION_ID, authorizationId);
    }

    public boolean isFirstFetch() {
        return !DONE.equals(persistentStorage.get(FIRST_FETCH_FLAG));
    }

    public void markFirstFetchAsDone() {
        persistentStorage.put(FIRST_FETCH_FLAG, DONE);
    }
}
