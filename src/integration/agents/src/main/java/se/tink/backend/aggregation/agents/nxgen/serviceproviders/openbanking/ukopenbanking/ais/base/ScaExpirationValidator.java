package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base;

import java.time.LocalDateTime;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ScaExpirationValidator {

    public static final String LAST_SCA_TIME = "last_SCA_time";

    private final PersistentStorage persistentStorage;
    private final long limitInMinutes;

    public ScaExpirationValidator(PersistentStorage persistentStorage, long limitInMinutes) {
        if (limitInMinutes <= 0) {
            throw new IllegalArgumentException(
                    "The limitInMinutes constraint should be higher than Zero");
        }
        this.persistentStorage = persistentStorage;
        this.limitInMinutes = limitInMinutes;
    }

    public ScaStatus evaluateStatus() {
        return isScaExpired() ? ScaStatus.EXPIRED : ScaStatus.VALID;
    }

    public boolean isScaExpired() {
        return restoreLastScaTime()
                .map(lastSca -> hasXMinutesPassed(limitInMinutes, lastSca))
                .orElse(Boolean.TRUE);
    }

    public long getLimitInMinutes() {
        return limitInMinutes;
    }

    private boolean hasXMinutesPassed(long limitInMinutes, LocalDateTime time) {
        // Subtracting 5 seconds - just in case
        return LocalDateTime.now().isAfter(time.plusMinutes(limitInMinutes).minusSeconds(5));
    }

    private Optional<LocalDateTime> restoreLastScaTime() {
        return persistentStorage.get(LAST_SCA_TIME, String.class).map(LocalDateTime::parse);
    }
}
