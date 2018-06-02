package se.tink.backend.common.mail.monthly.summary;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.common.mail.monthly.summary.utils.PeriodUtils;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class PeriodUtilsTest {

    @Test
    public void currentMonthIfNothingIsAdded() {
        UserProfile profile = new UserProfile();
        profile.setPeriodMode(ResolutionTypes.MONTHLY);

        PeriodUtils calculator = new PeriodUtils(profile);

        String currentMonthPeriod = DateUtils.getCurrentMonthPeriod();

        Assert.assertEquals(currentMonthPeriod, calculator.getMonthPeriodFromToday(Months.months(0)));
    }

    @Test
    public void previousMonthIfOneMonthIsSubtracted() {
        UserProfile profile = new UserProfile();
        profile.setPeriodMode(ResolutionTypes.MONTHLY);

        PeriodUtils calculator = new PeriodUtils(profile);

        Assert.assertEquals(getMonthPeriod(-1), calculator.getMonthPeriodFromToday(Months.months(-1)));
    }

    @Test
    public void monthBeforePreviousMonthIfTwoMonthsAreSubtracted() {
        UserProfile profile = new UserProfile();
        profile.setPeriodMode(ResolutionTypes.MONTHLY);

        PeriodUtils calculator = new PeriodUtils(profile);

        Assert.assertEquals(getMonthPeriod(-2), calculator.getMonthPeriodFromToday(Months.months(-2)));
    }

    @Test
    @Ignore // This test needs to be fixed by either injection or not running on some specific days.
    public void sucessIfTodayIsFirstDayInPeriod() {
        UserProfile profile = new UserProfile();
        profile.setPeriodMode(ResolutionTypes.MONTHLY_ADJUSTED);
        profile.setPeriodAdjustedDay(getCurrentDay());

        PeriodUtils calculator = new PeriodUtils(profile);

        Assert.assertEquals(true, calculator.isTodayFirstDayInCurrentPeriod());
    }

    /**
     * Since this method uses getCurrentOrPreviousBusinessDay a few steps down in its implementation it was failing
     * if we added +1 day for adjustedDay if new Date() -> Friday. Thus we set the adjusted day to +10 instead
     * to more accurately utilize the test.
     */
    @Test
    @Ignore // This test needs to be fixed by either injection or not running on some specific days.
    public void falseIfTodayIsNotFirstDayInPeriod() {
        UserProfile profile = new UserProfile();
        profile.setPeriodMode(ResolutionTypes.MONTHLY_ADJUSTED);
        profile.setPeriodAdjustedDay(getCurrentDay() + 10); // Should be a day that makes new Date() not considered as a first day

        PeriodUtils calculator = new PeriodUtils(profile);

        Assert.assertEquals(false, calculator.isTodayFirstDayInCurrentPeriod());
    }

    private String getMonthPeriod(int delta) {
        DateTime dateTime = new DateTime().plusMonths(delta);
        return ThreadSafeDateFormat.FORMATTER_MONTHLY.format(dateTime.toDate());
    }

    private int getCurrentDay() {
        return DateTime.now().getDayOfMonth();
    }

}
