package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import java.time.LocalDateTime;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.SibsSignSteps;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.Consent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SibsUserState {

    private static final String CONSENT_ID = "CONSENT_ID";
    private static final String SIBS_MANUAL_AUTHENTICATION_IN_PROGRESS =
            "sibs_manual_authentication_in_progress";

    private final PersistentStorage persistentStorage;

    SibsUserState(final PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public String getConsentId() {
        return getConsent().getConsentId();
    }

    public Consent getConsent() {
        return persistentStorage
                .get(CONSENT_ID, Consent.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public void removeConsent() {
        persistentStorage.remove(CONSENT_ID);
    }

    public void startManualAuthentication(final ConsentResponse consentResponse) {
        Consent consent =
                new Consent(consentResponse.getConsentId(), LocalDateTime.now().toString());
        persistentStorage.put(SIBS_MANUAL_AUTHENTICATION_IN_PROGRESS, true);
        persistentStorage.put(CONSENT_ID, consent);
    }

    public boolean isManualAuthenticationInProgress() {
        return persistentStorage
                .get(SibsSignSteps.SIBS_MANUAL_AUTHENTICATION_IN_PROGRESS, Boolean.class)
                .orElse(false);
    }

    public void finishManualAuthentication() {
        persistentStorage.put(SibsSignSteps.SIBS_MANUAL_AUTHENTICATION_IN_PROGRESS, false);
    }
}
