package se.tink.backend.system.workers.statistics;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class StatisticsGeneratiorWorkerTest {

    @Test
    public void testCleanDataPeriodGeneration1() throws ParseException {
        String today = ThreadSafeDateFormat.FORMATTER_DAILY.format(new Date());
        Date todayDate = ThreadSafeDateFormat.FORMATTER_DAILY.parse(today);

        String expectedDate = ThreadSafeDateFormat.FORMATTER_MONTHLY.format(new Date());
        List<String> correctList = new ArrayList<String>();

        correctList.add(expectedDate);

        List<String> validCleanDatesList = DateUtils
                .createPeriodList(todayDate, todayDate, ResolutionTypes.MONTHLY, -1);

        validateResult(correctList, validCleanDatesList);
    }

    @Test
    public void testCleanDataPeriodGeneration2() throws ParseException {
        Date oldDate = ThreadSafeDateFormat.FORMATTER_DAILY.parse("2013-01-06");

        Date todayDate = ThreadSafeDateFormat.FORMATTER_DAILY.parse("2013-02-15");
        List<String> correctList = new ArrayList<String>();
        correctList.add("2013-01");
        correctList.add("2013-02");

        List<String> validCleanDatesList = DateUtils.createPeriodList(oldDate, todayDate, ResolutionTypes.MONTHLY, -1);

        validateResult(correctList, validCleanDatesList);
    }

    @Test
    public void testCleanDataPeriodGeneration3() throws ParseException {
        Date oldDate = ThreadSafeDateFormat.FORMATTER_DAILY.parse("2012-02-02");
        Date todayDate = ThreadSafeDateFormat.FORMATTER_DAILY.parse("2013-02-15");

        List<String> correctList = new ArrayList<String>();
        correctList.add("2012-02");
        correctList.add("2012-03");
        correctList.add("2012-04");
        correctList.add("2012-05");
        correctList.add("2012-06");
        correctList.add("2012-07");
        correctList.add("2012-08");
        correctList.add("2012-09");
        correctList.add("2012-10");
        correctList.add("2012-11");
        correctList.add("2012-12");
        correctList.add("2013-01");
        correctList.add("2013-02");

        List<String> validCleanDatesList = DateUtils.createPeriodList(oldDate, todayDate, ResolutionTypes.MONTHLY, -1);

        validateResult(correctList, validCleanDatesList);
    }

    @Test
    public void testCleanDataPeriodGeneration4() throws ParseException {
        Date oldDate = ThreadSafeDateFormat.FORMATTER_DAILY.parse("2011-02-02");
        Date todayDate = ThreadSafeDateFormat.FORMATTER_DAILY.parse("2013-02-15");

        List<String> correctList = new ArrayList<String>();
        correctList.add("2011-02");
        correctList.add("2011-03");
        correctList.add("2011-04");
        correctList.add("2011-05");
        correctList.add("2011-06");
        correctList.add("2011-07");
        correctList.add("2011-08");
        correctList.add("2011-09");
        correctList.add("2011-10");
        correctList.add("2011-11");
        correctList.add("2011-12");
        correctList.add("2012-01");
        correctList.add("2012-02");
        correctList.add("2012-03");
        correctList.add("2012-04");
        correctList.add("2012-05");
        correctList.add("2012-06");
        correctList.add("2012-07");
        correctList.add("2012-08");
        correctList.add("2012-09");
        correctList.add("2012-10");
        correctList.add("2012-11");
        correctList.add("2012-12");
        correctList.add("2013-01");
        correctList.add("2013-02");

        List<String> validCleanDatesList = DateUtils.createPeriodList(oldDate, todayDate, ResolutionTypes.MONTHLY, -1);

        validateResult(correctList, validCleanDatesList);
    }

    private void validateResult(List<String> correctList, List<String> validCleanDatesList) {

        Assert.assertEquals("Not correct numer of items in list", correctList.size(), validCleanDatesList.size());

        for (int i = 0; i < validCleanDatesList.size(); i++) {
            System.out.println(validCleanDatesList.get(i));
            Assert.assertEquals("Element number " + i + " are not equal. Is " + correctList.get(i) + " but shoul be "
                    + validCleanDatesList.get(i), correctList.get(i), validCleanDatesList.get(i));
        }
    }
    
    @Test
    public void testCalcIncValue() {
        System.out.println("Max 1");
        for (double i = 0; i <= 2; i = i + 0.1) {
            System.out.println("\t" + i + " - " + StatisticsGeneratorWorker.calculatePositiveFunctionValue(i, 1));
        }
        
        System.out.println("Max 14");
        for (double i = 0; i <= 20; i++) {
            System.out.println("\t" + i + " - " + StatisticsGeneratorWorker.calculatePositiveFunctionValue(i, 14));
        }
        
        System.out.println("Max 30");
        for (double i = 0; i <= 40; i++) {
            System.out.println("\t" + i + " - " + StatisticsGeneratorWorker.calculateNegativeFunctionValue(i, 30));
        }
    }

}
