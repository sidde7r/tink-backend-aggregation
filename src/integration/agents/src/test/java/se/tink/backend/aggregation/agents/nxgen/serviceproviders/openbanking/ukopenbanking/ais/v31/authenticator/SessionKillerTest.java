package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SessionKillerTest {

    @Test
    public void shouldCleanUpAndExpireSession() {
        // given
        PersistentStorage storage = new PersistentStorage();
        storage.put(
                UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCOUNT_CONSENT_ID, "DUMMY_ID");
        storage.put(
                UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCESS_TOKEN, "DUMMY_TOKEN");
        String DUMMY_ERROR_MSG = "DUMMY ERROR MSG";

        // expected
        assertThatExceptionOfType(SessionException.class)
                .isThrownBy(() -> SessionKiller.cleanUpAndExpireSession(storage, DUMMY_ERROR_MSG))
                .withMessage(DUMMY_ERROR_MSG);
        assertThat(
                        storage.get(
                                UkOpenBankingV31Constants.PersistentStorageKeys
                                        .AIS_ACCOUNT_CONSENT_ID,
                                String.class))
                .isEmpty();
        assertThat(
                        storage.get(
                                UkOpenBankingV31Constants.PersistentStorageKeys.AIS_ACCESS_TOKEN,
                                String.class))
                .isEmpty();
    }
}
