package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.utils;

import com.google.api.client.util.Strings;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

public class FinTsDateParser {
    private static final int BOOKED_DATE_LENGTH = 4;
    private static final String INTEGER_DATE_COMPACT = "yyMMdd";
    private static final DateTimeFormatter FORMATTER_INTEGER_DATE_COMPACT = DateTimeFormatter.ofPattern(INTEGER_DATE_COMPACT);

    public static LocalDate parseDate(final String dateString) {
        String date = "";
        String bookedDate = "";
        try {
            date = dateString.substring(0, 6);
            bookedDate = dateString.substring(6, 10);
            LocalDate d = LocalDate.parse(date, FORMATTER_INTEGER_DATE_COMPACT);
            Optional<LocalDate> b = parseBookedDate(bookedDate, d);
            return b.orElse(d);

        } catch (Exception e) {
            return dateFallback(date);
        }
    }

    private static Optional<LocalDate> parseBookedDate(final String bookedDate, final LocalDate valueDate) {
        if (Strings.isNullOrEmpty(bookedDate) || bookedDate.length() != BOOKED_DATE_LENGTH) {
            return Optional.empty();
        }
        final int bookedMonth = Integer.parseInt(bookedDate.substring(0, 2));
        final int bookedDay = Integer.parseInt(bookedDate.substring(2, 4));
        int bookedYear = valueDate.getYear();
        if (bookedMonth > valueDate.getMonth().getValue()) {
             --bookedYear;
        }
        return Optional.of(LocalDate.of(bookedYear, bookedMonth, bookedDay));
    }

    //Sparkasse sometimes sends invalid dates such as 180229
    //This date is not valid since 2018 is not a leap year, and February 29 is a leap day
    private static LocalDate dateFallback(String date) {
        return LocalDate.parse(date, FORMATTER_INTEGER_DATE_COMPACT);
    }

    public static Date toDate(final LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
