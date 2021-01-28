package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.StorageKeys.CONSENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.StorageKeys.STATE;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class UnicreditPersistentStorage {

    private final PersistentStorage persistentStorage;

    public void saveConsentId(String consentId) {
        persistentStorage.put(CONSENT_ID, consentId);
    }

    public Optional<String> getConsentId() {
        return persistentStorage.get(CONSENT_ID, String.class);
    }

    public void removeConsentId() {
        persistentStorage.remove(CONSENT_ID);
    }

    public void saveAuthenticationState(String state) {
        persistentStorage.put(STATE, state);
    }

    public Optional<String> getAuthenticationState() {
        return persistentStorage.get(STATE, String.class);
    }

    public void saveScaRedirectUrlForPayment(String paymentId, String scaRedirectUrl) {
        persistentStorage.put(paymentId, scaRedirectUrl);
    }

    public Optional<String> getScaRedirectUrlForPayment(String paymentId) {
        return persistentStorage.get(paymentId, String.class);
    }
}
