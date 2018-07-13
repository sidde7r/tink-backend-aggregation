package se.tink.backend.aggregation.agents.banks.seb.utilities;

import com.google.common.base.Preconditions;
import java.util.Calendar;
import java.util.Date;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class SEBDateUtil {
    public static String nextPossibleTransferDate(Date now, boolean withinSEB) {
        Preconditions.checkNotNull(now);

        Date nextPossibleDate;
        if (withinSEB) {
            nextPossibleDate = now;
        } else  {
            nextPossibleDate = nextPossibleExternalDate(now);
        }

        return ThreadSafeDateFormat.FORMATTER_DAILY.format(nextPossibleDate);
    }

    private static Date nextPossibleExternalDate(Date now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);

        moveToNextMidnightIfAfterMiddayBreak(calendar);
        moveToNextBusinessDayIfNotBusinessDay(calendar);

        return calendar.getTime();
    }

    /**
     * SEB's Web Internet Bank: "Till andra svenska banker är pengarna tillgängliga för mottagaren samma dag om du skickar uppdraget senast 13.35."
     * ~ EN: "To other Swedish banks, the money are available for the recipient same day if you send the transfer before 13.35."
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

    /**
     * Cannot transfer on non-business days
     */
    private static void moveToNextBusinessDayIfNotBusinessDay(Calendar calendar) {
        while (!DateUtils.isBusinessDay(calendar)) {
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
