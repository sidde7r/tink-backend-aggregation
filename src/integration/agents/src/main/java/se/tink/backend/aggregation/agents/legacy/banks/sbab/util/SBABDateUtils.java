package se.tink.backend.aggregation.agents.banks.sbab.util;

import com.google.common.base.Preconditions;
import java.util.Calendar;
import java.util.Date;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class SBABDateUtils {

    public static String nextPossibleTransferDate(Date now, boolean isWithinSBAB) {
        Preconditions.checkNotNull(now);

        Date nextPossibleDate;
        if (isWithinSBAB) {
            nextPossibleDate = now;
        } else {
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
     * SBAB's Web Internet Bank: "Om du gör en överföring till ett konto utanför SBAB på en bankdag
     * innan kl. 13.00 förs pengarna över till mottagarens bank samma dag. Om du gör en överföring
     * en bankdag efter kl. 13.00 eller en helgdag kommer pengarna fram nästa bankdag."
     *
     * <p>~ EN: "If you make a transfer to an account outside of SBAB on a bank day before 13.00,
     * the money will be transferred to the recipient the same day, If you make a transfer on a bank
     * day after 13.00 or not on a bank day the money will arrive the next bank day."
     */
    private static void moveToNextMidnightIfAfterMiddayBreak(Calendar calendar) {
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

        if (isAfterMiddayBreak(currentHour)) {
            moveToNextMidnight(calendar);
        }
    }

    private static boolean isAfterMiddayBreak(int hour) {
        return hour >= 13;
    }

    private static void moveToNextBusinessDayIfNotBusinessDay(Calendar calendar) {
        while (!DateUtils.isBusinessDay(calendar)) {
            moveToNextMidnight(calendar);
        }
    }

    private static void moveToNextMidnight(Calendar calendar) {
        calendar.add(Calendar.DATE, 1);
        DateUtils.setInclusiveStartTime(calendar);
    }
}
