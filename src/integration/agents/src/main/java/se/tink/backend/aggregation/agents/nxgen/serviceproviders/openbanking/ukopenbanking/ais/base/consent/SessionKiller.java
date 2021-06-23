package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.consent;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public interface SessionKiller {

    static void cleanUpAndExpireSession(PersistentStorage storage, SessionException exception) {
        storage.remove(PersistentStorageKeys.AIS_ACCOUNT_CONSENT_ID);
        storage.remove(PersistentStorageKeys.AIS_ACCESS_TOKEN);
        storage.remove(PersistentStorageKeys.AIS_ACCOUNT_CONSENT_CREATION_DATE);
        throw exception;
    }
}
