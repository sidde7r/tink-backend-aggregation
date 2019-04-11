package se.tink.backend.aggregation.agents.nxgen.es.banks.ing;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public abstract class IngUtils {

    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(IngConstants.DATE_PATTERN);

    public static Date toJavaLangDate(LocalDate localDate) {
        return new Date(localDate.atStartOfDay(IngConstants.ZONE_ID).toInstant().toEpochMilli());
    }

    public static Date toJavaLangDate(String dateAsString) {
        return toJavaLangDate(LocalDate.parse(dateAsString, DATE_FORMATTER));
    }
}
