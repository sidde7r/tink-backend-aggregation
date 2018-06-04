package se.tink.backend.common.utils;

import java.text.ParseException;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StringDoublePair;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class DataUtilsTest {

    public List<StringDoublePair> zeroFillForResolution(ResolutionTypes resolution, List<StringDoublePair> series) {
        
        List<StringDoublePair> result = DataUtils.zeroFill(series, resolution);        
        for (StringDoublePair pair : result) {
            System.out.println(pair.getKey() + "\t" + pair.getValue());
        }
        System.out.println("");
        return result;
    }
    
    public List<Statistic> flatFillForResolution(ResolutionTypes resolution, List<Statistic> series) {
        List<Statistic> result = DataUtils.flatFill(Lists.newArrayList(series), resolution, false);
        for (Statistic s : result) {
            System.out.println(s.getPeriod() + "\t" + s.getValue());
        }
        System.out.println("");
        return result;
    }
    
    @Test
    public void testZeroFillMonthly() {
        System.out.println("testZeroFillMonthly");
        List<StringDoublePair> series = Lists.newArrayList();
        
        StringDoublePair par1 = new StringDoublePair("2014-01", 2);
        series.add(par1);
        StringDoublePair par2 = new StringDoublePair("2014-12", 2);
        series.add(par2);
        
        List<StringDoublePair> result = zeroFillForResolution(ResolutionTypes.MONTHLY, series);
        
        Assert.assertEquals("Incorrect length", 12, result.size(), 0);
        
        for (StringDoublePair pair : result) {
            if (pair.getKey().equals("2014-01") || pair.getKey().equals("2014-12")) {
                Assert.assertEquals("Incorrect value", 2, pair.getValue(), 0);
            } else {
                Assert.assertEquals("Incorrect value", 0, pair.getValue(), 0);
            }
        }
    }
    
    @Test
    public void testZeroFillDaily() {
        System.out.println("testZeroFillDaily");
        List<StringDoublePair> series = Lists.newArrayList();
        
        StringDoublePair par1 = new StringDoublePair("2014-01-01", 2);
        series.add(par1);
        StringDoublePair par2 = new StringDoublePair("2014-12-31", 2);
        series.add(par2);
        
        List<StringDoublePair> result = zeroFillForResolution(ResolutionTypes.DAILY, series);
        
        Assert.assertEquals("Incorrect length", 365, result.size(), 0);
        
        for (StringDoublePair pair : result) {
            if (pair.getKey().equals("2014-01-01") || pair.getKey().equals("2014-12-31")) {
                Assert.assertEquals("Incorrect value", 2, pair.getValue(), 0);
            } else {
                Assert.assertEquals("Incorrect value", 0, pair.getValue(), 0);
            }
        }
    }
    
    @Test
    public void testZeroFillYearly() {
        System.out.println("testZeroFillYearly");
        List<StringDoublePair> series = Lists.newArrayList();
        
        StringDoublePair par1 = new StringDoublePair("2012", 2);
        series.add(par1);
        StringDoublePair par2 = new StringDoublePair("2014", 2);
        series.add(par2);
        
        List<StringDoublePair> result = zeroFillForResolution(ResolutionTypes.YEARLY, series);
        
        Assert.assertEquals("Incorrect length", 3, result.size(), 0);
        
        for (StringDoublePair pair : result) {
            if (pair.getKey().equals("2012") || pair.getKey().equals("2014")) {
                Assert.assertEquals("Incorrect value", 2, pair.getValue(), 0);
            } else {
                Assert.assertEquals("Incorrect value", 0, pair.getValue(), 0);
            }
        }
    }
    
    @Test
    public void testZeroFillWeekly() {
        System.out.println("testZeroFillweekly");
        List<StringDoublePair> series = Lists.newArrayList();
        
        StringDoublePair par1 = new StringDoublePair("2014:03", 2);
        series.add(par1);
        StringDoublePair par2 = new StringDoublePair("2014:52", 2);
        series.add(par2);
        
        List<StringDoublePair> result = zeroFillForResolution(ResolutionTypes.WEEKLY, series);
        
        Assert.assertEquals("Incorrect length", 50, result.size(), 0);
        
        for (StringDoublePair pair : result) {
            if (pair.getKey().equals("2014:03") || pair.getKey().equals("2014:52")) {
                Assert.assertEquals("Incorrect value", 2, pair.getValue(), 0);
            } else {
                Assert.assertEquals("Incorrect value", 0, pair.getValue(), 0);
            }
        }
    }
    
    @Test
    public void testFlatFillMontly() {
        System.out.println("testZeroPadMontly");
        Statistic stats1 = new Statistic();
        stats1.setPeriod("2014-01");
        stats1.setValue(2);
        
        Statistic stats2 = new Statistic();
        stats2.setPeriod("2014-04");
        stats2.setValue(4);
        
        Statistic stats3 = new Statistic();
        stats3.setPeriod("2014-12");
        stats3.setValue(6);
        
        List<Statistic> series = Lists.newArrayList();
        series.add(stats1);
        series.add(stats2);
        series.add(stats3);

        List<Statistic> statistics = flatFillForResolution(ResolutionTypes.MONTHLY, series);
        Assert.assertEquals("Incorrect length", 12, statistics.size(), 0);

        for (Statistic s : statistics) {
            if (s.getPeriod().equals("2014-01") || s.getPeriod().equals("2014-03")) {
                Assert.assertEquals(2, s.getValue(), 0);
            }
            if (s.getPeriod().equals("2014-04") || s.getPeriod().equals("2014-06")) {
                Assert.assertEquals(4, s.getValue(), 0);
            }
            if (s.getPeriod().equals("2014-12")) {
                Assert.assertEquals(6, s.getValue(), 0);
            }
        }
    }
    
    @Test
    public void testFlatFilYearly() {
        System.out.println("testZeroPadyearly");
        Statistic stats1 = new Statistic();
        stats1.setPeriod("2010");
        stats1.setValue(2);
        
        Statistic stats2 = new Statistic();
        stats2.setPeriod("2012");
        stats2.setValue(4);
        
        Statistic stats3 = new Statistic();
        stats3.setPeriod("2014");
        stats3.setValue(6);
        
        List<Statistic> series = Lists.newArrayList();
        series.add(stats1);
        series.add(stats2);
        series.add(stats3);
        
        List<Statistic> statistics = flatFillForResolution(ResolutionTypes.YEARLY, series);
        Assert.assertEquals("Incorrect length", 5, statistics.size(), 0);
        
        for (Statistic s : statistics) {
            if (s.getPeriod().equals("2010") || s.getPeriod().equals("2011")) {
                Assert.assertEquals(2, s.getValue(), 0);
            }
            if (s.getPeriod().equals("2012")) {
                Assert.assertEquals(4, s.getValue(), 0);
            }
            if (s.getPeriod().equals("2014")) {
                Assert.assertEquals(6, s.getValue(), 0);
            }
        }
    }
    
    @Test
    public void testflatFillDaily() {
        System.out.println("testZeroPadDaily");
        Statistic stats1 = new Statistic();
        stats1.setPeriod("2014-01-01");
        stats1.setValue(2);
        
        Statistic stats2 = new Statistic();
        stats2.setPeriod("2014-02-01");
        stats2.setValue(4);
        
        Statistic stats3 = new Statistic();
        stats3.setPeriod("2014-09-12");
        stats3.setValue(6);
        
        List<Statistic> series = Lists.newArrayList();
        series.add(stats1);
        series.add(stats2);
        series.add(stats3);
        
        List<Statistic> statistics = flatFillForResolution(ResolutionTypes.DAILY, series);
        Assert.assertEquals("Incorrect length", 255, statistics.size(), 0);
        
        for (Statistic s : statistics) {
            if (s.getPeriod().equals("2014-01-01") || s.getPeriod().equals("2014-01-12")) {
                Assert.assertEquals(2, s.getValue(), 0);
            }
            if (s.getPeriod().equals("2014-02-01") || s.getPeriod().equals("2014-02-22")) {
                Assert.assertEquals(4, s.getValue(), 0);
            }
            if (s.getPeriod().equals("2014-09-12")) {
                Assert.assertEquals(6, s.getValue(), 0);
            }
        }
    }
    
    @Test
    public void testFlatFillWeekly() {
        System.out.println("testZeroPadDaily");
        Statistic stats1 = new Statistic();
        stats1.setPeriod("2014:03");
        stats1.setValue(2);
        
        Statistic stats2 = new Statistic();
        stats2.setPeriod("2014:05");
        stats2.setValue(4);
        
        Statistic stats3 = new Statistic();
        stats3.setPeriod("2014:50");
        stats3.setValue(6);
        
        List<Statistic> series = Lists.newArrayList();
        series.add(stats1);
        series.add(stats2);
        series.add(stats3);
        
        List<Statistic> statistics = flatFillForResolution(ResolutionTypes.WEEKLY, series);
        Assert.assertEquals("Incorrect length", 48, statistics.size(), 0);
        
        for (Statistic s : statistics) {
            if (s.getPeriod().equals("2014-03") || s.getPeriod().equals("2014-04")) {
                Assert.assertEquals(2, s.getValue(), 0);
            }
            if (s.getPeriod().equals("2014-05") || s.getPeriod().equals("2014-26")) {
                Assert.assertEquals(4, s.getValue(), 0);
            }
            if (s.getPeriod().equals("2014-50")) {
                Assert.assertEquals(6, s.getValue(), 0);
            }
        }
    }

    @Test
    public void testForTempFixForFlatFillWeekly() {
        System.out.println("testZeroPadDaily");

        Statistic stats1 = new Statistic();
        stats1.setPeriod("2016:50");
        stats1.setValue(4);

        Statistic stats2 = new Statistic();
        stats2.setPeriod("2016:52");
        stats2.setValue(6);

        Statistic stats3 = new Statistic();
        stats3.setPeriod("2016:53");
        stats3.setValue(7);

        List<Statistic> series = Lists.newArrayList();
        series.add(stats1);
        series.add(stats2);
        series.add(stats3);

        List<Statistic> statistics = flatFillForResolution(ResolutionTypes.WEEKLY, series);

        // With temp fix should have statitsics for 50, 51, 52 (and not 53)
        Assert.assertEquals("Incorrect length", 3, statistics.size(), 0);
    }

    @Test
    public void testTempFixForFlatFillWeeklyWithNoAccountBalanceHistory() {
        System.out.println("testZeroPadDaily");

        Statistic stats1 = new Statistic();
        stats1.setPeriod("2016:53");
        stats1.setValue(7);

        List<Statistic> series = Lists.newArrayList();
        series.add(stats1);

        List<Statistic> statistics = flatFillForResolution(ResolutionTypes.WEEKLY, series);

        // With temp fix should be empty
        Assert.assertEquals("Incorrect length", 0, statistics.size(), 0);
    }
    
    @Test
    public void testPadMonthly() {
        List<StringDoublePair> series = Lists.newArrayList();
        StringDoublePair par1 = new StringDoublePair("2014-02", 2);
        series.add(par1);
        StringDoublePair par2 = new StringDoublePair("2014-11", 2);
        series.add(par2);
        
        Set<String> keys = Sets.newHashSet("2014-03");
        List<StringDoublePair> result = DataUtils.pad(series, keys);
        
        Assert.assertTrue("Incirrect lenght", result.size() > 0);
        
        for (StringDoublePair pair : result) {
            System.out.println(pair.getKey() + "\t" + pair.getValue());
            if (pair.getKey().equals("2014-03")) {
                Assert.assertEquals(0, pair.getValue(), 0);
            } else {
                Assert.assertEquals(2, pair.getValue(), 0);
            }
        }
    }

    @Test
    public void testFlatFillFromTodayUntilToday() throws ParseException {

        DateTime today = new DateTime();

        Statistic stats1 = new Statistic();
        stats1.setPeriod(ThreadSafeDateFormat.FORMATTER_DAILY.format(today.toDate()));

        List<Statistic> statistics = DataUtils.flatFillUntilToday(stats1, ResolutionTypes.DAILY);

        // Should not get any values since we are on the same day as today
        Assert.assertEquals(0, statistics.size());
    }

    @Test
    public void testFlatFillYesterdayUntilToday() throws ParseException {

        DateTime yesterday = new DateTime().minusDays(1); // yesterday

        Statistic stats1 = new Statistic();
        stats1.setPeriod(ThreadSafeDateFormat.FORMATTER_DAILY.format(yesterday.toDate()));

        List<Statistic> statistics = DataUtils.flatFillUntilToday(stats1, ResolutionTypes.DAILY);

        // Should get one value for today
        Assert.assertEquals(1, statistics.size());
        Assert.assertTrue(DateUtils.isSameDay(new DateTime().toDate(),
                ThreadSafeDateFormat.FORMATTER_DAILY.parse(statistics.get(0).getPeriod())));
    }

    @Test
    public void testFlatFill10DaysAgo() throws ParseException {

        DateTime tenDaysAgo = new DateTime().minusDays(10); // ten days ago

        Statistic stats1 = new Statistic();
        stats1.setPeriod(ThreadSafeDateFormat.FORMATTER_DAILY.format(tenDaysAgo.toDate()));

        List<Statistic> statistics = DataUtils.flatFillUntilToday(stats1, ResolutionTypes.DAILY);

        Assert.assertEquals(10, statistics.size());
    }

}    
