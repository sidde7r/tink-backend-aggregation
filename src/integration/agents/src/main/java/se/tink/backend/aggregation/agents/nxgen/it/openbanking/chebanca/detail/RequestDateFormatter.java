package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail;

import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.DateConfig.REQUEST_DATE_TIME_FMT;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RequestDateFormatter {
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern(REQUEST_DATE_TIME_FMT);

    public static String getDateFormatted(LocalDateTime date) {
        return date.format(formatter);
    }
}
