package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@AllArgsConstructor
public class DnbStorage {

    private static final String CONSENT_ID_KEY = "consentId";

    private final PersistentStorage persistentStorage;

    public void storeConsentId(String consentId) {
        persistentStorage.put(CONSENT_ID_KEY, consentId);
    }

    public String getConsentId() {
        return persistentStorage.get(CONSENT_ID_KEY);
    }

    public boolean containsConsentId() {
        return persistentStorage.containsKey(CONSENT_ID_KEY);
    }
}
