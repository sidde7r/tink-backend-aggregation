package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.storage.Storage;

public class RedsysConsentStorage {
    private final Storage storage;

    public RedsysConsentStorage(Storage storage) {
        this.storage = storage;
    }

    public String getConsentId() {
        return storage.get(StorageKeys.CONSENT_ID);
    }

    private ZonedDateTime getConsentValidFrom() {
        return ZonedDateTime.parse(
                storage.get(StorageKeys.CONSENT_VALID_FROM),
                DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public boolean consentIsNewerThan(long amount, TemporalUnit unit) {
        final ZonedDateTime consentDate = getConsentValidFrom();
        final ZonedDateTime targetDate = ZonedDateTime.now().minus(amount, unit);
        return consentDate.isAfter(targetDate);
    }

    public void useConsentId(String consentId) {
        storage.put(
                RedsysConstants.StorageKeys.CONSENT_VALID_FROM,
                ZonedDateTime.now(ZoneId.of("Europe/Madrid"))
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        storage.put(RedsysConstants.StorageKeys.CONSENT_ID, consentId);
    }
}
