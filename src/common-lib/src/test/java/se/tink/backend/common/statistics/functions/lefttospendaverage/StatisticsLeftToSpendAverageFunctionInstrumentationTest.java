package se.tink.backend.common.statistics.functions.lefttospendaverage;

import com.google.common.collect.Lists;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Range;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.date.ThreadSafeDateFormat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StatisticsLeftToSpendAverageFunctionInstrumentationTest {
    private static final String USER_ID = "auserid";

    private static final int AVERAGE_MONTHS_FOR_CALCULATION = 6;
    private static final int NUMBER_OF_MONTHS = 20;
    private static final int TRANSACTIONS_PER_DAY = 3;

    private static final int NUMBER_OF_STATISTICS_OBJECTS = TRANSACTIONS_PER_DAY * 30 * NUMBER_OF_MONTHS;

    private static final DateTime END_DATE = new DateTime(new GregorianCalendar(2016, 1, 31, 0, 0, 0));
    private static final DateTime START_DATE = END_DATE.minusMonths(NUMBER_OF_MONTHS);

    private static final Range<Double> VALID_TRANSACTION_VALUES = Range.closed(-20000.0, 20000.0);

    private static final Random random = new Random(1); // Seed same every time to generate same test samples every time

    private PrintWriter fileWriter;

    @Before
    public void setupInstance() throws FileNotFoundException {
        String date = new Date().toString();
        fileWriter = new PrintWriter(
                "log/StatisticsLeftToSpendAverageFunctionInstrumentationTest_instrumentStatisticsFunction_" + date + ".log");
    }

    @After
    public void closeStreams() throws IOException {
        fileWriter.close();
    }

    @Test
    @Ignore
    public void instrumentStatisticsFunction() {
        User user = stubUser();

        StatisticsLeftToSpendAverageFunction leftToSpendAverageFunction = new StatisticsLeftToSpendAverageFunction(
                ResolutionTypes.MONTHLY, user, AVERAGE_MONTHS_FOR_CALCULATION);

        List<Stopwatch> stopwatches = Lists.newArrayList();
        Iterable<Statistic> result = null;
        Collection<Statistic> statistics = null;
        for (int i = 0; i < 300; i++) {
            statistics = stubRandomStatistics();

            Stopwatch stopwatch = Stopwatch.createStarted();
            result = leftToSpendAverageFunction.apply(statistics);
            stopwatch.stop();

            stopwatches.add(stopwatch);
        }

        // Print a sample of statistics to be able to check it out in e.g. Excel
        printStatistics(statistics);
        printStatistics(result);

        printWatchesAndAssertAcceptable(stopwatches);
    }

    private void printWatchesAndAssertAcceptable(List<Stopwatch> stopwatches) {
        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();

        fileWriter.println("sep=;");
        //fileStream.println("#;Elapsed ms");
        for (Stopwatch stopwatche : stopwatches) {
            long elapsed = stopwatche.elapsed(TimeUnit.MILLISECONDS);
            //fileStream.println(String.format(new Locale("sv", "SE"), "%d;%d", i, elapsed));
            descriptiveStatistics.addValue(elapsed);
        }

        double mean = descriptiveStatistics.getMean();

        fileWriter.println();
        fileWriter.println("Average elapsed;" + mean);
        fileWriter.println("25% percentile elapsed;" + descriptiveStatistics.getPercentile(25));
        fileWriter.println("50% percentile elapsed;" + descriptiveStatistics.getPercentile(50));
        fileWriter.println("75% percentile elapsed;" + descriptiveStatistics.getPercentile(75));
        fileWriter.println("90% percentile elapsed;" + descriptiveStatistics.getPercentile(90));
        fileWriter.println("95% percentile elapsed;" + descriptiveStatistics.getPercentile(95));
        fileWriter.println("Max elapsed;" + descriptiveStatistics.getMax());
        fileWriter.println("Min elapsed;" + descriptiveStatistics.getMin());
        fileWriter.println("Total elapsed (" + stopwatches.size() + " passes);" + descriptiveStatistics.getSum());

        assertThat(mean).isLessThan(100);
    }

    private void printStatistics(Iterable<Statistic> statistics) {
        fileWriter.println("sep=;");
        fileWriter.println("Period;Description;Value");
        for (Statistic s : statistics) {
            fileWriter.println(String.format(new Locale("sv", "SE"), "%s;%s;%.2f",
                    s.getPeriod(),
                    s.getDescription(),
                    s.getValue()));
        }
    }

    private Collection<Statistic> stubRandomStatistics() {
        List<Statistic> statistics = Lists.newArrayList();

        int number = 0;
        while (number < NUMBER_OF_STATISTICS_OBJECTS) {
            statistics.add(stubRandomStatistic());
            number++;
        }

        return statistics;
    }

    private Statistic stubRandomStatistic() {
        Statistic statistic = new Statistic();
        statistic.setResolution(ResolutionTypes.MONTHLY);
        statistic.setType("left-to-spend");
        statistic.setUserId(USER_ID);
        statistic.setPayload(null);

        Date randomDateWithinPeriods = createRandomDateWithinPeriods().toDate();
        statistic.setDescription(ThreadSafeDateFormat.FORMATTER_DAILY.format(randomDateWithinPeriods));
        statistic.setPeriod(ThreadSafeDateFormat.FORMATTER_MONTHLY.format(randomDateWithinPeriods));

        statistic.setValue(createRandomTransactionValue());

        return statistic;
    }

    private DateTime createRandomDateWithinPeriods() {
        double nextDouble = random.nextDouble();

        long min = START_DATE.toDate().getTime();
        long max = END_DATE.toDate().getTime();

        long randomMinOffset = (long)(nextDouble * ((double)(max - min)));
        long randomDateWithinInterval = min + randomMinOffset;

        return new DateTime(randomDateWithinInterval);
    }

    private double createRandomTransactionValue() {
        double nextDouble = random.nextDouble();

        double max = VALID_TRANSACTION_VALUES.upperEndpoint();
        double min = VALID_TRANSACTION_VALUES.lowerEndpoint();

        return min + ((max - min) * nextDouble);
    }

    private User stubUser() {
        UserProfile stubbedUserProfile = stubUserProfile();

        User user = mock(User.class);
        when(user.getProfile())
                .thenReturn(stubbedUserProfile);
        when(user.getId())
                .thenReturn(USER_ID);
        return user;
    }

    private UserProfile stubUserProfile() {
        UserProfile userProfile = mock(UserProfile.class);
        when(userProfile.getPeriodAdjustedDay())
                .thenReturn(1);
        return userProfile;
    }
}