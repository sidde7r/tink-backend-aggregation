package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base;

import java.time.LocalDateTime;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ScaExpirationValidator {

    private final PersistentStorage persistentStorage;
    private final long limitInMinutes;
    public static final String LAST_SCA_TIME = "last_SCA_time";

    public ScaExpirationValidator(PersistentStorage persistentStorage, long limitInMinutes) {
        this.persistentStorage = persistentStorage;
        this.limitInMinutes = limitInMinutes;
    }

    public boolean isScaExpired() {
        return restoreLastScaTime()
                .map(lastSca -> hasXMinutesPassed(limitInMinutes, lastSca))
                .orElse(Boolean.FALSE);
    }

    private boolean hasXMinutesPassed(long limitInMinutes, LocalDateTime time) {
        // Subtracting 5 seconds - just in case
        return LocalDateTime.now().isAfter(time.plusMinutes(limitInMinutes).minusSeconds(5));
    }

    Optional<LocalDateTime> restoreLastScaTime() {
        return persistentStorage.get(LAST_SCA_TIME, String.class).map(LocalDateTime::parse);
    }
}
