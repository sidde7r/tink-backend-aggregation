package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SessionKiller {

    public static void cleanUpAndExpireSession(
            PersistentStorage storage, SessionException exception) {
        storage.remove(UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCOUNT_CONSENT_ID);
        storage.remove(UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCESS_TOKEN);
        throw exception;
    }
}
