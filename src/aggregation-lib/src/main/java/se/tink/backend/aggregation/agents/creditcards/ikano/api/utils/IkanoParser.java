package se.tink.backend.aggregation.agents.creditcards.ikano.api.utils;

import se.tink.libraries.date.ThreadSafeDateFormat;

import java.text.ParseException;
import java.util.Date;

public class IkanoParser {

    public static Double stringToDouble(String value) {
        return Double.parseDouble(value.replace(",", "."));
    }

    public static Date stringToDate(String date) throws ParseException {
        return ThreadSafeDateFormat.FORMATTER_DAILY.parse(date);
    }
}
