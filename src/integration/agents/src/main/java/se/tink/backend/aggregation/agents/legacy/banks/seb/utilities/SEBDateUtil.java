package se.tink.backend.aggregation.agents.banks.seb.utilities;

import com.google.common.base.Preconditions;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class SEBDateUtil {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");
    private static final Locale DEFAULT_LOCALE = new Locale("sv", "SE");
    private static final CountryDateHelper dateHelper =
            new CountryDateHelper(DEFAULT_LOCALE, TimeZone.getTimeZone(DEFAULT_ZONE_ID));
    private static final TimeZone TIME_ZONE_CET = TimeZone.getTimeZone("CET");

    public static String nextPossibleTransferDate(Date date, boolean withinSEB) {
        Preconditions.checkNotNull(date);
        Date nextPossibleDate = withinSEB ? date : nextPossibleExternalDate(date);
        return ThreadSafeDateFormat.FORMATTER_DAILY.format(nextPossibleDate);
    }

    private static Date nextPossibleExternalDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TIME_ZONE_CET);
        calendar.setTime(date);

        moveToNextMidnightIfAfterMiddayBreak(calendar);
        moveToNextBusinessDayIfNotBusinessDay(calendar);

        return calendar.getTime();
    }

    /**
     * SEB's Web Internet Bank: "Till andra svenska banker är pengarna tillgängliga för mottagaren
     * samma dag om du skickar uppdraget senast 13.35." ~ EN: "To other Swedish banks, the money are
     * available for the recipient same day if you send the transfer before 13.35 CET."
     */
    private static void moveToNextMidnightIfAfterMiddayBreak(Calendar calendar) {
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        if (isAfterMiddayBreak(currentHour, currentMinute)) {
            moveToNextMidnight(calendar);
        }
    }

    private static boolean isAfterMiddayBreak(int hour, int minute) {
        return hour > 13 || (hour == 13 && minute > 35);
    }

    /** Cannot transfer on non-business days */
    private static void moveToNextBusinessDayIfNotBusinessDay(Calendar calendar) {
        while (!dateHelper.isBusinessDay(calendar)) {
            moveToNextMidnight(calendar);
        }
    }

    private static void moveToNextMidnight(Calendar calendar) {
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}
