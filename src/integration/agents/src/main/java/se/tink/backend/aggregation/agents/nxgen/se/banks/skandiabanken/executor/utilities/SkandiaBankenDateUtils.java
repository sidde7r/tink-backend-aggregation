package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.utilities;

import java.time.Clock;
import java.util.Calendar;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.assertj.core.util.VisibleForTesting;
import se.tink.libraries.date.CountryDateHelper;

@RequiredArgsConstructor
public class SkandiaBankenDateUtils {

    private final CountryDateHelper dateHelper;

    public Date getTransferDateForBgPg(Date providedDate) {
        Date date = dateHelper.getProvidedDateOrBestPossibleDate(providedDate, 9, 00);
        return setTimeToMidnight(date);
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

    @VisibleForTesting
    public void setClockForTesting(Clock clockForTesting) {
        dateHelper.setClock(clockForTesting);
    }
}
