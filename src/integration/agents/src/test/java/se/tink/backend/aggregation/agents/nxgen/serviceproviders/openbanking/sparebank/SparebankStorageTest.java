package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(JUnitParamsRunner.class)
public class SparebankStorageTest {

    private SparebankStorage storage;

    @Before
    public void setup() {
        storage = new SparebankStorage(new PersistentStorage());
    }

    @Test
    public void shouldBeTooOldForFullFetchWhenNoTimestampStored() {
        // given nothing, empty storage

        // when
        boolean storedConsentTooOldForFullFetch = storage.isStoredConsentTooOldForFullFetch();

        // then
        assertThat(storedConsentTooOldForFullFetch).isTrue();
    }

    @Test
    @Parameters(method = "timestampAndExpectedResult")
    public void shouldJudgeCorrectlyBasedOnStoredTimestamp(
            long creationTimestamp, boolean expectedToBeTooOld) {
        // given
        storage.storeConsentCreationTimestamp(creationTimestamp);

        // when
        boolean storedConsentTooOldForFullFetch = storage.isStoredConsentTooOldForFullFetch();

        // then
        assertThat(storedConsentTooOldForFullFetch).isEqualTo(expectedToBeTooOld);
    }

    @SuppressWarnings("unused")
    private static Object timestampAndExpectedResult() {
        return new Object[] {
            new Object[] {toMilli(LocalDateTime.now()), false},
            new Object[] {toMilli(LocalDateTime.now().minusMinutes(53)), false},
            new Object[] {toMilli(LocalDateTime.now().minusMinutes(54)), false},
            new Object[] {toMilli(LocalDateTime.now().minusMinutes(55)), true},
            new Object[] {toMilli(LocalDateTime.now().minusMinutes(56)), true},
            new Object[] {toMilli(LocalDateTime.now().minusDays(1)), true},
            new Object[] {toMilli(LocalDateTime.now().minusYears(1)), true},
        };
    }

    private static long toMilli(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
