package se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public final class ActualLocalDateTimeSource implements LocalDateTimeSource {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }

    @Override
    public LocalDateTime now(ZoneId zoneId) {
        return LocalDateTime.now(zoneId);
    }

    @Override
    public ZonedDateTime nowZonedDateTime(ZoneId zoneId) {
        return ZonedDateTime.now(ZoneOffset.UTC);
    }

    @Override
    public Instant getInstant() {
        return Instant.now();
    }

    @Override
    public Instant getInstant(ZoneId zoneId) {
        return Instant.now(Clock.system(zoneId));
    }
}
