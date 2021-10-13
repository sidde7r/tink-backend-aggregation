package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.TriodosApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.TriodosConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@AllArgsConstructor
public final class ConsentStatusFetcher {
    private final PersistentStorage persistentStorage;
    private final TriodosApiClient client;

    public void throwSessionErrorIfInvalidConsent() throws SessionException {
        if (isConsentInvalidOrExpired()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    public boolean isConsentInvalidOrExpired() {
        String consentId = persistentStorage.get(BerlinGroupConstants.StorageKeys.CONSENT_ID);

        if (Strings.isNullOrEmpty(consentId)) {
            return false;
        }

        return TriodosConstants.ConsentStatus.INVALID_OR_EXPIRED.contains(
                client.getConsentStatus(consentId).getConsentStatus());
    }
}
