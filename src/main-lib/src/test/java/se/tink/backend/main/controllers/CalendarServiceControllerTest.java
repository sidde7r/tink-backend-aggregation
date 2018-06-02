package se.tink.backend.main.controllers;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.main.providers.calendar.PeriodListProvider;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.date.ThreadSafeDateFormat;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class CalendarServiceControllerTest {

    @Test
    public void testCreatingListOfPeriodsForAYear_withoutUserStatePeriods() {
        List<String> monthsToGetPeriodsFor = DateUtils.createPeriodListForYear(2016, ResolutionTypes.MONTHLY_ADJUSTED, 25);

        List<Period> userStatePeriods = Lists.newArrayList();

        PeriodListProvider periodListProvider = new PeriodListProvider();
        List<Period> periods = periodListProvider.buildListOfPeriods(userStatePeriods, monthsToGetPeriodsFor, ResolutionTypes.MONTHLY_ADJUSTED, 25);

        /* Test size of periods */
        int expected = monthsToGetPeriodsFor.size();
        int actual = periods.size();
        Assert.assertEquals(expected, actual);

        /* Test name of period so that it equals from monthsToGetPeriodsFor */
        for (int i=0; i<monthsToGetPeriodsFor.size(); i++) {
            String expectedPeriod = monthsToGetPeriodsFor.get(i);
            String actualPeriod = periods.get(i).getName();
            Assert.assertEquals(expectedPeriod, actualPeriod);
        }
    }

    @Test
    public void testCreatingListOfPeriodsForAYear_withUserStatePeriodsFromJanToAug() {
        List<String> monthsToGetPeriodsFor = DateUtils.createPeriodListForYear(2016, ResolutionTypes.MONTHLY_ADJUSTED, 25);

        List<Period> userStatePeriods = getPeriodsFromStartAndEndDate("2016-01-01", "2016-08-01", ResolutionTypes.MONTHLY_ADJUSTED);

        PeriodListProvider periodListProvider = new PeriodListProvider();
        List<Period> periods = periodListProvider.buildListOfPeriods(userStatePeriods, monthsToGetPeriodsFor, ResolutionTypes.MONTHLY_ADJUSTED, 25);

        /* Test size of periods */
        int expected = monthsToGetPeriodsFor.size();
        int actual = periods.size();
        Assert.assertEquals(expected, actual);

        /* Test name of period so that it equals from monthsToGetPeriodsFor */
        for (int i=0; i<monthsToGetPeriodsFor.size(); i++) {
            String expectedPeriod = monthsToGetPeriodsFor.get(i);
            String actualPeriod = periods.get(i).getName();
            Assert.assertEquals(expectedPeriod, actualPeriod);
        }
    }

    @Test
    public void testCreatingListOfPeriodsForAYear_withUserStatePeriodsFromJunToAug() {
        List<String> monthsToGetPeriodsFor = DateUtils.createPeriodListForYear(2016, ResolutionTypes.MONTHLY_ADJUSTED, 25);

        List<Period> userStatePeriods = getPeriodsFromStartAndEndDate("2016-06-01", "2016-08-01", ResolutionTypes.MONTHLY_ADJUSTED);

        PeriodListProvider periodListProvider = new PeriodListProvider();
        List<Period> periods = periodListProvider.buildListOfPeriods(userStatePeriods, monthsToGetPeriodsFor, ResolutionTypes.MONTHLY_ADJUSTED, 25);

        /* Test size of periods */
        int expected = monthsToGetPeriodsFor.size();
        int actual = periods.size();
        Assert.assertEquals(expected, actual);

        /* Test name of period so that it equals from monthsToGetPeriodsFor */
        for (int i=0; i<monthsToGetPeriodsFor.size(); i++) {
            String expectedPeriod = monthsToGetPeriodsFor.get(i);
            String actualPeriod = periods.get(i).getName();
            Assert.assertEquals(expectedPeriod, actualPeriod);
        }
    }

    @Test
    public void testCreatingListOfPeriodsForAPeriod_usingUserStatePeriodsFromJanToAug() {
        List<String> monthsToGetPeriodsFor = Lists.newArrayList();
        monthsToGetPeriodsFor.add("2016-03");

        List<Period> userStatePeriods = getPeriodsFromStartAndEndDate("2016-01-01", "2016-08-01", ResolutionTypes.MONTHLY_ADJUSTED);

        PeriodListProvider periodListProvider = new PeriodListProvider();
        List<Period> periods = periodListProvider.buildListOfPeriods(userStatePeriods, monthsToGetPeriodsFor, ResolutionTypes.MONTHLY_ADJUSTED, 25);

        /* Test size of periods */
        int expected = monthsToGetPeriodsFor.size();
        int actual = periods.size();
        Assert.assertEquals(expected, actual);

        /* Test name of period so that it equals from monthsToGetPeriodsFor */
        for (int i=0; i<monthsToGetPeriodsFor.size(); i++) {
            String expectedPeriod = monthsToGetPeriodsFor.get(i);
            String actualPeriod = periods.get(i).getName();
            Assert.assertEquals(expectedPeriod, actualPeriod);

            /* Only periods from userState is clean */
            Assert.assertEquals(true, periods.get(i).isClean());
        }
    }

    @Test
    public void testCreatingListOfPeriodsForAPeriod_notUsingUserStatePeriodsFromJanToAug() {
        List<String> monthsToGetPeriodsFor = Lists.newArrayList();
        monthsToGetPeriodsFor.add("2016-10");

        List<Period> userStatePeriods = getPeriodsFromStartAndEndDate("2016-01-01", "2016-08-01", ResolutionTypes.MONTHLY_ADJUSTED);

        PeriodListProvider periodListProvider = new PeriodListProvider();
        List<Period> periods = periodListProvider.buildListOfPeriods(userStatePeriods, monthsToGetPeriodsFor, ResolutionTypes.MONTHLY_ADJUSTED, 25);

        /* Test size of periods */
        int expected = monthsToGetPeriodsFor.size();
        int actual = periods.size();
        Assert.assertEquals(expected, actual);

        /* Test name of period so that it equals from monthsToGetPeriodsFor */
        for (int i=0; i<monthsToGetPeriodsFor.size(); i++) {
            String expectedPeriod = monthsToGetPeriodsFor.get(i);
            String actualPeriod = periods.get(i).getName();
            Assert.assertEquals(expectedPeriod, actualPeriod);

            /* Only periods from userState is clean */
            Assert.assertEquals(false, periods.get(i).isClean());
        }
    }

    @Test
    public void testValidYear_trueFor2016() {
        Integer year = 2016;

        PeriodListProvider periodListProvider = new PeriodListProvider();

        boolean expectedValue = true;
        boolean isYearValid = periodListProvider.isValidYear(year);
        Assert.assertEquals(expectedValue, isYearValid);
    }

    @Test
    public void testValidYear_trueFor0() {
        Integer year = 0;

        PeriodListProvider periodListProvider = new PeriodListProvider();

        boolean expectedValue = true;
        boolean isYearValid = periodListProvider.isValidYear(year);
        Assert.assertEquals(expectedValue, isYearValid);
    }

    @Test
    public void testValidYear_falseForMinus1() {
        Integer year = -1;

        PeriodListProvider periodListProvider = new PeriodListProvider();

        boolean expectedValue = false;
        boolean isYearValid = periodListProvider.isValidYear(year);
        Assert.assertEquals(expectedValue, isYearValid);
    }

    @Test
    public void testValidYear_falseForNull() {
        Integer year = null;

        PeriodListProvider periodListProvider = new PeriodListProvider();

        boolean expectedValue = false;
        boolean isYearValid = periodListProvider.isValidYear(year);
        Assert.assertEquals(expectedValue, isYearValid);
    }

    @Test
    public void testValidMonth_trueFor1() {
        Integer month = 1;

        PeriodListProvider periodListProvider = new PeriodListProvider();

        boolean expectedValue = true;
        boolean isMonth = periodListProvider.isValidMonth(month);
        Assert.assertEquals(expectedValue, isMonth);
    }

    @Test
    public void testValidMonth_trueFor12() {
        Integer month = 12;

        PeriodListProvider periodListProvider = new PeriodListProvider();

        boolean expectedValue = true;
        boolean isMonth = periodListProvider.isValidMonth(month);
        Assert.assertEquals(expectedValue, isMonth);
    }

    @Test
    public void testValidMonth_falseFor13() {
        Integer month = 13;

        PeriodListProvider periodListProvider = new PeriodListProvider();

        boolean expectedValue = false;
        boolean isMonth = periodListProvider.isValidMonth(month);
        Assert.assertEquals(expectedValue, isMonth);
    }

    @Test
    public void testValidMonth_falseFor0() {
        Integer month = 0;

        PeriodListProvider periodListProvider = new PeriodListProvider();

        boolean expectedValue = false;
        boolean isMonth = periodListProvider.isValidMonth(month);
        Assert.assertEquals(expectedValue, isMonth);
    }

    @Test
    public void testValidMonthfMonthsGreaterThanZero_falseFor0() {
        Integer month = 0;

        PeriodListProvider periodListProvider = new PeriodListProvider();

        boolean expectedValue = false;
        boolean isMonth = periodListProvider.isValidNumberOfMonthsGreaterThanZero(month);
        Assert.assertEquals(expectedValue, isMonth);
    }

    @Test
    public void testValidMonthfMonthsGreaterThanZero_trueFor2014() {
        Integer month = 2014;

        PeriodListProvider periodListProvider = new PeriodListProvider();

        boolean expectedValue = true;
        boolean isMonth = periodListProvider.isValidNumberOfMonthsGreaterThanZero(month);
        Assert.assertEquals(expectedValue, isMonth);
    }

    @Test
    public void testValidMonthsLessThanTwoYears_trueFor16() {
        Integer month = 16;

        PeriodListProvider periodListProvider = new PeriodListProvider();

        boolean expectedValue = true;
        boolean isMonth = periodListProvider.isValidNumberOfMonthsLessThanTwoYears(month);
        Assert.assertEquals(expectedValue, isMonth);
    }

    @Test
    public void testValidMonthsLessThanTwoYears_falseFor25() {
        Integer month = 25;

        PeriodListProvider periodListProvider = new PeriodListProvider();

        boolean expectedValue = false;
        boolean isMonth = periodListProvider.isValidNumberOfMonthsLessThanTwoYears(month);
        Assert.assertEquals(expectedValue, isMonth);
    }

    private List<Period> getPeriodsFromStartAndEndDate(String startDate, String endDate, ResolutionTypes resolutionType) {
        List<String> monthPeriodsUserStateToCreate = getMonthPeriodsUserStateToCreate(startDate, endDate);

        List<Period> userStatePeriods = Lists.newArrayList();
        for (String period : monthPeriodsUserStateToCreate) {
            Period p = new Period();
            p.setName(period);
            p.setResolution(resolutionType);
            p.setClean(true);
            p.setStartDate(getDateFromString(startDate));
            p.setEndDate(getDateFromString(endDate));
            userStatePeriods.add(p);
        }

        return userStatePeriods;
    }

    private List<String> getMonthPeriodsUserStateToCreate(String startDate, String endDate) {
        Date startD = getDateFromString(startDate);
        Date endD = getDateFromString(endDate);
        return DateUtils.createPeriodList(startD, endD, ResolutionTypes.MONTHLY_ADJUSTED, 25);
    }

    private Date getDateFromString(String date) {
        Date d = null;
        try {
            d = ThreadSafeDateFormat.FORMATTER_DAILY.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return d;
    }

    @Test
    public void testYearRegexp() {
        checkMatch(CalendarServiceController.YEAR, "2015");
        checkUnMatch(CalendarServiceController.YEAR, "2015-");
    }

    @Test
    public void testYearMonthOneRegexp() {
        checkUnMatch(CalendarServiceController.YEAR_MONTH_SINGLE, "2015");
        checkUnMatch(CalendarServiceController.YEAR_MONTH_SINGLE, "2015-01");
        checkUnMatch(CalendarServiceController.YEAR_MONTH_SINGLE, "2015-01-12");
        checkMatch(CalendarServiceController.YEAR_MONTH_SINGLE, "2015-1");
    }

    @Test
    public void testYearMonthTwoRegexp() {
        checkUnMatch(CalendarServiceController.YEAR_MONTH_DOOUBLE, "2015");
        checkUnMatch(CalendarServiceController.YEAR_MONTH_DOOUBLE, "2015-1");
        checkUnMatch(CalendarServiceController.YEAR_MONTH_DOOUBLE, "2015-1-12");
        checkUnMatch(CalendarServiceController.YEAR_MONTH_DOOUBLE, "2015-01-12");
        checkMatch(CalendarServiceController.YEAR_MONTH_DOOUBLE, "2015-01");
    }

    @Test
    public void testDayRegexp() {
        checkUnMatch(CalendarServiceController.DAY, "2015");
        checkUnMatch(CalendarServiceController.DAY, "2015-1");
        checkUnMatch(CalendarServiceController.DAY, "2015-1-12");
        checkUnMatch(CalendarServiceController.DAY, "2015-01");
        checkUnMatch(CalendarServiceController.DAY, "2015-01-15-");
        checkMatch(CalendarServiceController.DAY, "2015-01-12");
    }

    private void checkUnMatch(String regExp, String period) {
        Assert.assertFalse(period.matches(regExp));
    }

    private void checkMatch(String regExp, String period) {
        Assert.assertTrue(period.matches(regExp));
    }

}
