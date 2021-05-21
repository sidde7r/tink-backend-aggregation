package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.TriodosApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@AllArgsConstructor
public final class ConsentStatusFetcher {
    private final PersistentStorage persistentStorage;
    private final TriodosApiClient client;

    public boolean isConsentValid() {
        String consentId = persistentStorage.get(BerlinGroupConstants.StorageKeys.CONSENT_ID);

        if (Strings.isNullOrEmpty(consentId)) {
            return false;
        }

        final String consentStatus = client.getConsentStatus(consentId).getConsentStatus();

        return "valid".equalsIgnoreCase(consentStatus);
    }
}
