package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.storage.AuthenticationDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.storage.ConsentDataStorage;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public interface SessionKiller {

    static void cleanUpAndExpireSession(PersistentStorage storage, SessionException exception) {
        AuthenticationDataStorage authDataStorage = new AuthenticationDataStorage(storage);
        ConsentDataStorage consentDataStorage = new ConsentDataStorage(storage);

        consentDataStorage.removeConsentId();
        authDataStorage.removeAccessToken();
        authDataStorage.removeStrongAuthenticationTime();

        throw exception;
    }
}
