package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class FabricAutoAuthenticator implements AutoAuthenticator {

    private final PersistentStorage persistentStorage;
    private final FabricApiClient apiClient;

    @Override
    public void autoAuthenticate() {
        persistentStorage
                .get(StorageKeys.CONSENT_ID, String.class)
                .filter(this::isConsentValid)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }

    private boolean isConsentValid(String consentId) {
        return apiClient.getConsentStatus(consentId).getConsentStatus().isValid();
    }
}
