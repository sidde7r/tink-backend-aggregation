package se.tink.backend.common.statistics.functions;

import com.google.common.base.Strings;
import java.util.function.Function;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

import java.util.Date;
import java.util.Locale;
import java.util.Calendar;

/**
 * Transaction periodization function (WEEKLY)
 * 
 */
public class WeeklyPeriodizationFunction implements Function<Date, String> {
    private Locale locale;

    public WeeklyPeriodizationFunction(String locale) {
        this.locale = Catalog.getLocale(locale);
    }

    @Override
    public String apply(Date d) {
        Calendar calendar = DateUtils.getCalendar(locale);
        calendar.setTime(d);

        // JodaTime uses ISO week rules (week starts on monday even in US). We want week to start on
        // Sunday for US users so that's why Calendar is used.
        return ThreadSafeDateFormat.FORMATTER_WEEK_YEARLY.format(d) + ":"
                + Strings.padStart(Integer.toString(calendar.get(Calendar.WEEK_OF_YEAR)), 2, '0');
    }
}
