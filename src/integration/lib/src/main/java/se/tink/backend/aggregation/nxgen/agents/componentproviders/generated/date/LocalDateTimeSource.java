package se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public interface LocalDateTimeSource {

    /**
     * Returns current local date time
     *
     * @deprecated This method is no longer acceptable to compute current local date time
     *     <p>Use {@link ActualLocalDateTimeSource#now(ZoneId)} instead.
     * @return current local date time
     */
    @Deprecated
    LocalDateTime now();

    /**
     * Returns current instant
     *
     * @deprecated This method is no longer acceptable to compute current instant
     *     <p>Use {@link ActualLocalDateTimeSource#getInstant(ZoneId)} instead.
     * @return current instant
     */
    @Deprecated
    Instant getInstant();

    default long getSystemCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    LocalDateTime now(ZoneId zoneId);

    Instant getInstant(ZoneId zoneId);
}
