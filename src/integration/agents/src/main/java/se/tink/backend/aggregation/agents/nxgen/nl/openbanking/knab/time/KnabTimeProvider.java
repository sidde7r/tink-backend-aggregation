package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.time;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;

@RequiredArgsConstructor
public class KnabTimeProvider {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z");

    private static final ZoneId ZONE_ID = TimeZone.getTimeZone("GMT").toZoneId();

    private final LocalDateTimeSource localDateTimeSource;

    public LocalDate date() {
        return localDateTimeSource.now(ZoneId.systemDefault()).toLocalDate();
    }

    public String formatted() {
        return FORMATTER.format(zonedDateTime());
    }

    private ZonedDateTime zonedDateTime() {
        return ZonedDateTime.ofInstant(localDateTimeSource.getInstant(ZONE_ID), ZONE_ID);
    }
}
