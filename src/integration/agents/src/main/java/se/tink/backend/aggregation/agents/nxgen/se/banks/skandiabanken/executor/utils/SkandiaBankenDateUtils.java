package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.utils;

import java.time.Clock;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.assertj.core.util.VisibleForTesting;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.DateFormatting;
import se.tink.libraries.date.CountryDateHelper;

public class SkandiaBankenDateUtils {

    private static final CountryDateHelper dateHelper =
            new CountryDateHelper(
                    DateFormatting.LOCALE, TimeZone.getTimeZone(DateFormatting.ZONE_ID));

    public Date getTransferDateForBgPg(Date providedDate) {
        Date date = dateHelper.getProvidedDateOrBestPossibleDate(providedDate, 9, 00);
        return setTimeToMidnight(date);
    }

    public boolean isAfterCutOffWithPaymentDateBeforeCutOff(Date paymentDate) {
        Date todayCurrentTime = dateHelper.getNowAsDate();
        Date todayCutOffTime = getTodayAtCutOffTime();

        if (todayCurrentTime.before(todayCutOffTime)) {
            return false;
        }

        return paymentDate.before(todayCutOffTime);
    }

    /**
     * The Skandia app sends date in the format yyyy-MM-dd'T'HH:mm:ss.SSSZ. Time is always set to
     * 00:00:00.000 no matter when the payment is made. This method will set the time of given date
     * to midnight.
     */
    private Date setTimeToMidnight(Date date) {
        Calendar calendar = dateHelper.getCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date getTodayAtCutOffTime() {
        Date now = dateHelper.getNowAsDate();

        Calendar calendar = dateHelper.getCalendar();
        calendar.setTime(now);
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    @VisibleForTesting
    public static void setClockForTesting(Clock clockForTesting) {
        dateHelper.setClock(clockForTesting);
    }
}
