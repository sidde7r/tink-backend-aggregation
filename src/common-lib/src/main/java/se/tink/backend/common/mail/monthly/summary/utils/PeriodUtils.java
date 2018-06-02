package se.tink.backend.common.mail.monthly.summary.utils;

import com.google.common.base.Preconditions;
import org.joda.time.DateTime;
import org.joda.time.ReadablePeriod;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

import java.util.Date;

/**
 * Utility class for period related functionality used when sending emails
 */
public class PeriodUtils {

    private UserProfile userProfile;

    public PeriodUtils(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    /**
     * Return true if today is the first business day in the current period.
     *
     * @return
     */
    public boolean isTodayFirstDayInCurrentPeriod() {
        return isFirstDayInCurrentPeriod(DateUtils.getToday());
    }

    /**
     * Return true if input date is the first day in current period
     */
    public boolean isFirstDayInCurrentPeriod(Date date) {
        Preconditions.checkNotNull(userProfile.getPeriodMode());

        String currentPeriod = UserProfile.ProfileDateUtils.getCurrentMonthPeriod(userProfile);

        Date firstDateInPeriod = UserProfile.ProfileDateUtils.getFirstDateFromPeriod(currentPeriod, userProfile);

        return DateUtils.isSameDay(date, firstDateInPeriod);
    }


    /**
     * Returns a month period from today with the readablePeriod parameter added or subtracted
     *
     * @param readablePeriod
     * @return
     */
    public String getMonthPeriodFromToday(ReadablePeriod readablePeriod) {
        Preconditions.checkNotNull(userProfile.getPeriodMode());

        String currentPeriod = UserProfile.ProfileDateUtils.getCurrentMonthPeriod(userProfile);

        DateTime dateTime = DateTime.parse(currentPeriod).plus(readablePeriod);

        return ThreadSafeDateFormat.FORMATTER_MONTHLY.format(dateTime.toDate());

    }

}