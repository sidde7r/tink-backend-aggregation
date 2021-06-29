package se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class ConsorsbankStorage {

    private static final String CONSENT_ID = "consentId";
    private static final String CONSENT_ACCESS = "consentAccess";

    private final PersistentStorage persistentStorage;

    public void saveConsentId(String consentId) {
        persistentStorage.put(CONSENT_ID, consentId);
    }

    public String getConsentId() {
        return persistentStorage.get(CONSENT_ID);
    }

    public void saveConsentAccess(AccessEntity accessEntity) {
        persistentStorage.put(CONSENT_ACCESS, accessEntity);
    }

    public AccessEntity getConsentAccess() {
        return persistentStorage
                .get(CONSENT_ACCESS, AccessEntity.class)
                .orElse(AccessEntity.builder().build());
    }
}
