package se.tink.backend.combined.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.combined.AbstractServiceIntegrationTest;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.statistics.StatisticsGeneratorAggregator;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StatisticQuery;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserContext;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.UserState;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.libraries.date.ThreadSafeDateFormat;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * TODO this is a unit test
 */
public class StatisticsServiceIntegrationTest extends AbstractServiceIntegrationTest {

    protected static final ObjectMapper mapper = new ObjectMapper();

    private static final Ordering<Statistic> ACCOUNT_HISTORY_ORDERING = new Ordering<Statistic>() {
        @Override
        public int compare(Statistic left, Statistic right) {
            return ComparisonChain.start().compare(left.getPeriod(), right.getPeriod()).result();
        }
    };

    static {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    User testUserUpdatedDate, testUserOldDate;

    @Before
    public void setUp() throws Exception {
        testUserUpdatedDate = registerTestUserWithDemoCredentialsAndData();
        testUserOldDate = registerTestUserWithDemoCredentialsAndData("anv1ud");
    }

    @Ignore
    @Test
    public void testHammerModificationsAndWaitForStatistics() throws InterruptedException {
        Random random = new Random();

        StatisticQuery query = new StatisticQuery();
        query.setTypes(Collections.singletonList("income-and-expenses"));
        query.setResolution(ResolutionTypes.MONTHLY);

        List<Transaction> ts = serviceFactory.getTransactionService().list(testUserUpdatedDate, null, null, null, 0, 0, null, null);

        assertThat(ts.size(), is(not(0)));

        Transaction transaction = ts.get(0);
        transaction.setDescription(transaction.getDescription() + "-" + random.nextInt(123134));
        serviceFactory.getTransactionService().updateTransaction(testUserUpdatedDate, transaction.getId(), transaction);

        Thread.sleep(1000);

        List<Statistic> ss = serviceFactory.getStatisticsService().query(new AuthenticatedUser(
                HttpAuthenticationMethod.BASIC, testUserUpdatedDate), query);

        assertThat(ss.size(), is(not(0)));
    }

    @Test
    public void testLeftToSpendSinglePeriodStatistics() {

        Map<String, Double> resultMatrix = getOctoberResulMatrix();

        List<String> periods = Collections.singletonList("2012-10");
        List<Statistic> ss = testBurnDown(periods, ResolutionTypes.MONTHLY_ADJUSTED);

        List<String> days = Lists.newLinkedList();
        Set<String> uniqueMonths = Sets.newConcurrentHashSet();

        for (Statistic s : ss) {
            if (s.getType().equals(Statistic.Types.LEFT_TO_SPEND_AVERAGE)) {
                continue;
            }
            days.add(s.getDescription());
            uniqueMonths.add(s.getDescription());
            assertEquals("Sum of " + s.getDescription() + " is not correct",
                    resultMatrix.get(s.getDescription()), s.getValue(), 0);
        }
        assertTrue(days.size() == uniqueMonths.size());
    }

    @Test
    public void testLeftToSpendTwoPeriodStatistics() {
        List<String> periods = Lists.newArrayList("2012-09", "2012-10");
        List<Statistic> ss = testBurnDown(periods, ResolutionTypes.MONTHLY_ADJUSTED);

        Map<String, Double> resultMatrixOct = getOctoberResulMatrix();
        Map<String, Double> resultMatrixSep = getSeptemberResulMatrix();

        List<String> days = Lists.newLinkedList();
        Set<String> uniqueMonths = Sets.newConcurrentHashSet();

        for (Statistic s : ss) {
            if (s.getType().equals(Statistic.Types.LEFT_TO_SPEND_AVERAGE)) {
                continue;
            }
            days.add(s.getDescription());
            uniqueMonths.add(s.getDescription());
            if (s.getPeriod().equals("2012-10")) {
                assertEquals("Sum of " + s.getDescription() + " is not correct",
                        resultMatrixOct.get(s.getDescription()), s.getValue(), 0);
            } else {
                assertEquals("Sum of " + s.getDescription() + " is not correct",
                        resultMatrixSep.get(s.getDescription()), s.getValue(), 0);
            }
        }
        assertTrue(days.size() == uniqueMonths.size());
    }

    @Test
    public void testLeftToSpendFlatFillWithValueZeroMA_userHasExcludeAllTransactionsForAMonth() {

        List<String> periods = Collections.singletonList("2012-05");
        List<Statistic> ss = testBurnDown(periods, ResolutionTypes.MONTHLY_ADJUSTED);

        List<String> days = Lists.newLinkedList();
        Set<String> uniqueDays = Sets.newConcurrentHashSet();

        ImmutableListMultimap<String, Statistic> statisticsByType = Multimaps.index(ss,
                Statistic::getType);

        List<Statistic> leftToSpendStatsUnfiltered = statisticsByType.get(Statistic.Types.LEFT_TO_SPEND);

        /* Simulating user has excluded all transactions for this month by emptying the statistics list */
        ss = Lists.newArrayList();

        List<Statistic> statsToAdd = StatisticsGeneratorAggregator.getStatisticsForEmptyPeriod(leftToSpendStatsUnfiltered, ss);

        ss.addAll(statsToAdd);

        int expectedNumberOfDaysWithValueZero = ss.size();
        int actualNumberOfDaysWithValueZero = 0;

        for (Statistic s : ss) {
            days.add(s.getDescription());
            uniqueDays.add(s.getDescription());

            if (s.getValue() == 0.0) {
                actualNumberOfDaysWithValueZero++;
            }
        }

        assertTrue(days.size() == uniqueDays.size());

        assertEquals(expectedNumberOfDaysWithValueZero, actualNumberOfDaysWithValueZero);
    }

    @Test
    public void testLeftToSpendFlatFillWithValueZeroM_userHasExcludeAllTransactionsForAMonth() {

        List<String> periods = Collections.singletonList("2012-05");
        List<Statistic> ss = testBurnDown(periods, ResolutionTypes.MONTHLY);

        List<String> days = Lists.newLinkedList();
        Set<String> uniqueDays = Sets.newConcurrentHashSet();

        ImmutableListMultimap<String, Statistic> statisticsByType = Multimaps.index(ss,
                Statistic::getType);

        List<Statistic> leftToSpendStatsUnfiltered = statisticsByType.get(Statistic.Types.LEFT_TO_SPEND);

        /* Simulating user has excluded all transactions for this month by emptying the statistics list */
        ss = Lists.newArrayList();

        List<Statistic> statsToAdd = StatisticsGeneratorAggregator.getStatisticsForEmptyPeriod(leftToSpendStatsUnfiltered, ss);

        ss.addAll(statsToAdd);

        int expectedNumberOfDaysWithValueZero = ss.size();
        int actualNumberOfDaysWithValueZero = 0;

        for (Statistic s : ss) {
            days.add(s.getDescription());
            uniqueDays.add(s.getDescription());

            if (s.getValue() == 0.0) {
                actualNumberOfDaysWithValueZero++;
            }
        }

        assertTrue(days.size() == uniqueDays.size());

        assertEquals(expectedNumberOfDaysWithValueZero, actualNumberOfDaysWithValueZero);
    }

    private List<Statistic> testBurnDown(List<String> periods, ResolutionTypes resolutionType) {
        User user = serviceFactory.getUserService().getUser(testUserOldDate);

        String safeToSpend = Statistic.Types.LEFT_TO_SPEND;
        String safeToSpendAverage = Statistic.Types.LEFT_TO_SPEND_AVERAGE;
        List<String> typeList = Lists.newArrayList(safeToSpend, safeToSpendAverage);

        StatisticQuery query = new StatisticQuery();
        query.setTypes(typeList);
        query.setResolution(resolutionType);
        query.setPeriods(periods);

        List<Statistic> ss = serviceFactory.getStatisticsService().query(new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user), query);

        for (Statistic s : ss) {
            assertTrue(typeList.contains(s.getType()));
            assertTrue(ObjectUtils.equals(s.getResolution(), resolutionType));
            assertTrue(ObjectUtils.equals(s.getUserId(), user.getId()));
        }
        return ss;
    }

    private Map<String, Double> getOctoberResulMatrix() {
        Map<String, Double> resultMatrix = Maps.newHashMap();
        resultMatrix.put("2012-10-24", 96D);
        resultMatrix.put("2012-10-23", -115D);
        resultMatrix.put("2012-10-22", 104D);
        resultMatrix.put("2012-10-21", 144D);
        resultMatrix.put("2012-10-20", 144D);
        resultMatrix.put("2012-10-19", 144D);
        resultMatrix.put("2012-10-18", 239D);
        resultMatrix.put("2012-10-17", 329D);
        resultMatrix.put("2012-10-16", 409D);
        resultMatrix.put("2012-10-15", 409D);
        resultMatrix.put("2012-10-14", 1112D);
        resultMatrix.put("2012-10-13", 1112D);
        resultMatrix.put("2012-10-12", 1112D);
        resultMatrix.put("2012-10-11", 1219D);
        resultMatrix.put("2012-10-10", 1500D);
        resultMatrix.put("2012-10-09", 1589D);
        resultMatrix.put("2012-10-08", 2033D);
        resultMatrix.put("2012-10-07", 5210D);
        resultMatrix.put("2012-10-06", 5210D);
        resultMatrix.put("2012-10-05", 5210D);
        resultMatrix.put("2012-10-04", 5210D);
        resultMatrix.put("2012-10-03", 5305D);
        resultMatrix.put("2012-10-02", 5386D);
        resultMatrix.put("2012-10-01", 5485D);
        resultMatrix.put("2012-09-31", 5867D);
        resultMatrix.put("2012-09-30", 5867D);
        resultMatrix.put("2012-09-29", 5867D);
        resultMatrix.put("2012-09-28", 5867D);
        resultMatrix.put("2012-09-27", 13803D);
        resultMatrix.put("2012-09-26", 18747D);
        resultMatrix.put("2012-09-25", 22823D);
        return resultMatrix;
    }

    private Map<String, Double> getSeptemberResulMatrix() {
        Map<String, Double> resultMatrix = Maps.newHashMap();
        resultMatrix.put("2012-09-24", -162D);
        resultMatrix.put("2012-09-23", -438D);
        resultMatrix.put("2012-09-22", -438D);
        resultMatrix.put("2012-09-21", -438D);
        resultMatrix.put("2012-09-20", -291D);
        resultMatrix.put("2012-09-19", -13D);
        resultMatrix.put("2012-09-18", -13D);
        resultMatrix.put("2012-09-17", 587D);
        resultMatrix.put("2012-09-16", 108D);
        resultMatrix.put("2012-09-15", 108D);
        resultMatrix.put("2012-09-14", 108D);
        resultMatrix.put("2012-09-13", 2600D);
        resultMatrix.put("2012-09-12", 2720D);
        resultMatrix.put("2012-09-11", 2815D);
        resultMatrix.put("2012-09-10", 3130D);
        resultMatrix.put("2012-09-09", 4870D);
        resultMatrix.put("2012-09-08", 4870D);
        resultMatrix.put("2012-09-07", 4870D);
        resultMatrix.put("2012-09-06", 9603D);
        resultMatrix.put("2012-09-05", 9603D);
        resultMatrix.put("2012-09-04", 9713D);
        resultMatrix.put("2012-09-03", 9713D);
        resultMatrix.put("2012-09-02", 10563D);
        resultMatrix.put("2012-09-01", 10563D);
        resultMatrix.put("2012-08-31", 10563D);
        resultMatrix.put("2012-08-30", 10563D);
        resultMatrix.put("2012-08-29", 10670D);
        resultMatrix.put("2012-08-28", 10670D);
        resultMatrix.put("2012-08-27", 10870D);
        resultMatrix.put("2012-08-26", 18796D);
        resultMatrix.put("2012-08-25", 18796D);
        resultMatrix.put("2012-08-24", 18796D);
        return resultMatrix;
    }
}
