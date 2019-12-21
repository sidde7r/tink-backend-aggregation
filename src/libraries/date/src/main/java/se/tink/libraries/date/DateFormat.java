package se.tink.libraries.date;

import com.google.common.base.Strings;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public final class DateFormat {

    public static final String YEAR_MONTH_DAY = "yyyy-MM-dd";

    private DateFormat() {
        throw new AssertionError();
    }

    public static class Zone {
        public static final String UTC = "UTC";
        public static final String GMT = "GMT";
    }

    public static String formatDate(
            final Date date, final String dateFormat, final String timeZone) {
        final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

        if (!Strings.isNullOrEmpty(timeZone)) {
            sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
        }

        return sdf.format(date);
    }

    public static String formatDate(final Date date, final String timestampFormat) {
        final SimpleDateFormat sdf = new SimpleDateFormat(timestampFormat);
        return sdf.format(date);
    }

    public static String formatDateTime(
            final Date date, final String timestampFormat, final String timezone) {
        final SimpleDateFormat sdf = new SimpleDateFormat(timestampFormat);
        if (!Strings.isNullOrEmpty(timezone)) {
            sdf.setTimeZone(TimeZone.getTimeZone(timezone));
        }
        return sdf.format(date);
    }

    public static String getFormattedCurrentDate(final String format, final String timeZone) {
        return formatDate(Calendar.getInstance().getTime(), format, timeZone);
    }

    public static Date convertToDateViaInstant(LocalDate dateToConvert) {
        return java.util.Date.from(
                dateToConvert.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
