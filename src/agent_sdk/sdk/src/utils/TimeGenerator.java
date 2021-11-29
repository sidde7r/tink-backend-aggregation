package se.tink.agent.sdk.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

// TODO: can we derive ZoneId from e.g. Provider?
// TODO: or rather: we *SHOULD* derive ZoneId from some configuration, e.g. Provider.
public interface TimeGenerator {
    LocalDate localDateNow(ZoneId zoneId);

    LocalDateTime localDateTimeNow(ZoneId zoneId);

    Instant instantNow(ZoneId zoneId);
}
