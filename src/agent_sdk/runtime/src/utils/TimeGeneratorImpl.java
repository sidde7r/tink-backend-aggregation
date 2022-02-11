package se.tink.agent.runtime.utils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import se.tink.agent.sdk.utils.TimeGenerator;

public class TimeGeneratorImpl implements TimeGenerator {

    @Override
    public LocalDate localDateNow(ZoneId zoneId) {
        return LocalDate.now(zoneId);
    }

    @Override
    public LocalDateTime localDateTimeNow(ZoneId zoneId) {
        return LocalDateTime.now(zoneId);
    }

    @Override
    public Instant instantNow(ZoneId zoneId) {
        return Instant.now(Clock.system(zoneId));
    }
}
