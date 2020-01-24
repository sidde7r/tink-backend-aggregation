package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail;

import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.DateConfig.REQUEST_DATE_TIME_FMT;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

class RequestDateFormatter {
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern(REQUEST_DATE_TIME_FMT).withLocale(Locale.US);

    static String getDateFormatted(ZonedDateTime date) {
        return date.format(formatter);
    }
}
