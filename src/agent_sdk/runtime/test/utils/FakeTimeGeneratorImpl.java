package src.agent_sdk.runtime.test.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import se.tink.agent.sdk.utils.TimeGenerator;

public class FakeTimeGeneratorImpl implements TimeGenerator {

    private static final LocalDate CONSTANT_DATE = LocalDate.of(1992, 4, 10);
    private static final Instant CONSTANT_UTC_INSTANT =
            CONSTANT_DATE.atStartOfDay(ZoneOffset.UTC).toInstant();

    @Override
    public LocalDate localDateNow(ZoneId zoneId) {
        return CONSTANT_DATE;
    }

    @Override
    public LocalDateTime localDateTimeNow(ZoneId zoneId) {
        return LocalDateTime.ofInstant(CONSTANT_UTC_INSTANT, zoneId);
    }

    @Override
    public Instant instantNow(ZoneId zoneId) {
        return CONSTANT_DATE.atStartOfDay(zoneId).toInstant();
    }
}
