package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.storage.data;

import static com.google.common.base.Preconditions.checkNotNull;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public final class ConsentDataStorage {

    private final PersistentStorage persistentStorage;

    public ConsentDataStorage(PersistentStorage persistentStorage) {
        this.persistentStorage =
                checkNotNull(persistentStorage, "Persistent storage can not be null!");
    }

    public PersistentStorage getPersistentStorage() {
        return persistentStorage;
    }

    public void saveConsentId(String consentId) {
        saveConsentIdOrElseThrow(consentId, SessionError.CONSENT_EXPIRED::exception);
    }

    public void saveConsentIdOrElseThrow(
            String consentId, Supplier<? extends RuntimeException> exceptionSupplier) {
        String id = Optional.ofNullable(consentId).orElseThrow(exceptionSupplier);
        persistentStorage.put(PersistentStorageKeys.AIS_ACCOUNT_CONSENT_ID, id);
    }

    public String restoreConsentId() {
        return persistentStorage
                .get(PersistentStorageKeys.AIS_ACCOUNT_CONSENT_ID, String.class)
                .orElse(StringUtils.EMPTY);
    }

    public String restoreConsentIdOrElseThrow(
            Supplier<? extends RuntimeException> exceptionSupplier) {
        return persistentStorage
                .get(PersistentStorageKeys.AIS_ACCOUNT_CONSENT_ID, String.class)
                .orElseThrow(exceptionSupplier);
    }

    public void removeConsentId() {
        persistentStorage.remove(PersistentStorageKeys.AIS_ACCOUNT_CONSENT_ID);
    }

    public void saveConsentCreationDate(Instant creationDate) {
        checkNotNull(creationDate, "Consent creation date can not be null!");
        persistentStorage.put(
                PersistentStorageKeys.AIS_ACCOUNT_CONSENT_CREATION_DATE, creationDate);
    }

    public Instant restoreConsentCreationDate() {
        return restoreConsentCreationDateOrElseThrow(SessionError.CONSENT_INVALID::exception);
    }

    public Instant restoreConsentCreationDateOrElseThrow(
            Supplier<? extends RuntimeException> exceptionSupplier) {
        return persistentStorage
                .get(PersistentStorageKeys.AIS_ACCOUNT_CONSENT_CREATION_DATE, Instant.class)
                .orElseThrow(exceptionSupplier);
    }

    public void removeConsentCreationDate() {
        persistentStorage.remove(PersistentStorageKeys.AIS_ACCOUNT_CONSENT_CREATION_DATE);
    }
}
