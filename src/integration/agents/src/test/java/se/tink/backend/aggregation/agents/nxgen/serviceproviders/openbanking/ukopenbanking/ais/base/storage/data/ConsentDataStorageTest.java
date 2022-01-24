package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.storage.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ConsentDataStorageTest {

    private ConsentDataStorage consentDataStorage;
    private PersistentStorage persistentStorage;

    @Before
    public void setUp() throws Exception {
        persistentStorage = new PersistentStorage();
        consentDataStorage = new ConsentDataStorage(persistentStorage);
    }

    @Test
    public void shouldThrowNullPointerExceptionWhenPersistentStorageIsNull() {
        assertThatThrownBy(() -> new ConsentDataStorage(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Persistent storage can not be null!");
    }

    @Test
    public void shouldThrowNullPointerExceptionWhenConsentIdIsNull() {
        assertThatThrownBy(
                        () ->
                                consentDataStorage.saveConsentIdOrElseThrow(
                                        null, NullPointerException::new))
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void shouldThrowSessionErrorConsentExpiredExceptionWhenConsentIdIsNull() {
        assertThatThrownBy(() -> consentDataStorage.saveConsentId(null))
                .isExactlyInstanceOf(SessionError.CONSENT_EXPIRED.exception().getClass());
    }

    @Test
    public void shouldProperlyStoreConsentId() {
        // given
        String consentId = "dummyConsentId";

        // when
        consentDataStorage.saveConsentId(consentId);

        // then
        assertThat(persistentStorage).hasSize(1);
        assertThat(persistentStorage.values()).containsExactly("dummyConsentId");
    }

    @Test
    public void shouldRestoreConsentId() {
        // given
        String consentId = "dummyConsentId";
        consentDataStorage.saveConsentId(consentId);

        // when
        String restoredConsentID = consentDataStorage.restoreConsentId();

        // then
        assertThat(restoredConsentID).isEqualTo("dummyConsentId");
    }

    @Test
    public void shouldReturnEmptyStringWhenRestoredConsentIdIsNotAvailable() {
        // given
        // when
        String restoredConsentID = consentDataStorage.restoreConsentId();

        // then
        assertThat(restoredConsentID).isEmpty();
    }

    @Test
    public void shouldThrowProvidedExceptionWhenRestoredConsentIdIsNull() {
        // given
        // when
        ThrowingCallable throwingCallable =
                () ->
                        consentDataStorage.restoreConsentIdOrElseThrow(
                                SessionError.SESSION_EXPIRED::exception);

        // then
        assertThatThrownBy(throwingCallable)
                .isInstanceOf(SessionError.SESSION_EXPIRED.exception().getClass());
    }

    @Test
    public void shouldRemoveConsentIdFromTheStorage() {
        // given
        persistentStorage.put(PersistentStorageKeys.AIS_ACCOUNT_CONSENT_ID, "dummyConsentID");

        // when
        consentDataStorage.removeConsentId();

        // then
        assertThat(persistentStorage).isEmpty();
    }

    @Test
    public void shouldDoNotChangePersistentStorageStateWhileRemovingAbsentConsentId() {
        // given
        persistentStorage.put("dummyKey", "dummyValue");

        // when
        consentDataStorage.removeConsentId();

        // then
        assertThat(persistentStorage).hasSize(1);
    }

    @Test
    public void shouldThrowNullPointerExceptionWhenCreationDateIsNull() {
        assertThatThrownBy(() -> consentDataStorage.saveConsentCreationDate(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Consent creation date can not be null!");
    }

    @Test
    public void shouldProperlyStoreCreationDate() {
        // given
        Instant consentCreationDate = Instant.parse("2018-11-30T18:35:24.00Z");

        // when
        consentDataStorage.saveConsentCreationDate(consentCreationDate);

        // then
        assertThat(persistentStorage).hasSize(1);
        assertThat(persistentStorage.values().stream().map(Double::valueOf))
                .containsExactly(Double.valueOf("1543602924.0"));
    }

    @Test
    public void shouldProperlyRestoreConsentCreationDate() {
        // given
        Instant consentCreationDate = Instant.parse("2018-11-30T18:35:24.00Z");
        consentDataStorage.saveConsentCreationDate(consentCreationDate);

        // when
        Instant creationDate = consentDataStorage.restoreConsentCreationDate();

        // then
        assertThat(creationDate).isEqualTo(Instant.parse("2018-11-30T18:35:24.00Z"));
    }

    @Test
    public void shouldThrowSessionErrorConsentInvalidExceptionWhenCreationDateIsNotAvailable() {
        // given
        // when
        ThrowingCallable throwingCallable = () -> consentDataStorage.restoreConsentCreationDate();

        // then
        assertThatThrownBy(throwingCallable)
                .isExactlyInstanceOf(SessionError.CONSENT_INVALID.exception().getClass());
    }

    @Test
    public void shouldThrowCustomExceptionWhenCreationDateIsNotAvailable() {
        // given
        // when
        ThrowingCallable throwingCallable =
                () ->
                        consentDataStorage.restoreConsentCreationDateOrElseThrow(
                                SessionError.CONSENT_EXPIRED::exception);

        // then
        assertThatThrownBy(throwingCallable)
                .isExactlyInstanceOf(SessionError.CONSENT_EXPIRED.exception().getClass());
    }

    @Test
    public void shouldRemoveConsentCreationDateFromTheStorage() {
        // given
        Instant consentCreationDate = Instant.parse("2018-11-30T18:35:24.00Z");
        persistentStorage.put(
                PersistentStorageKeys.AIS_ACCOUNT_CONSENT_CREATION_DATE, consentCreationDate);

        // when
        consentDataStorage.removeConsentCreationDate();

        // then
        assertThat(persistentStorage).isEmpty();
    }

    @Test
    public void shouldDoNotChangePersistentStorageStateWhileRemovingAbsentConsentCreationDate() {
        // given
        persistentStorage.put("dummyKey", "dummyValue");

        // when
        consentDataStorage.removeConsentCreationDate();

        // then
        assertThat(persistentStorage).hasSize(1);
    }
}
