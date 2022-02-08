package se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public final class ConstantLocalDateTimeSource implements LocalDateTimeSource {

    private static final LocalDate CONSTANT_DATE = LocalDate.of(1992, 4, 10);
    private static final Instant CONSTANT_UTC_INSTANT =
            CONSTANT_DATE.atStartOfDay(ZoneOffset.UTC).toInstant();

    @Override
    public LocalDateTime now(ZoneId zoneId) {
        return LocalDateTime.ofInstant(getInstant(zoneId), zoneId);
    }

    @Override
    public ZonedDateTime nowZonedDateTime(ZoneId zoneId) {
        return ZonedDateTime.ofInstant(getInstant(), zoneId);
    }

    @Override
    public Instant getInstant(ZoneId zoneId) {
        return CONSTANT_DATE.atStartOfDay(zoneId).toInstant();
    }

    @Override
    public LocalDateTime now() {
        return LocalDateTime.ofInstant(getInstant(), ZoneOffset.UTC);
    }

    @Override
    public Instant getInstant() {
        return CONSTANT_UTC_INSTANT;
    }

    @Override
    public long getSystemCurrentTimeMillis() {
        return CONSTANT_UTC_INSTANT.toEpochMilli();
    }
}
