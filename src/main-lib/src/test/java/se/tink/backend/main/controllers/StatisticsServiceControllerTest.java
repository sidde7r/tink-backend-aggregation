package se.tink.backend.main.controllers;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import se.tink.backend.common.concurrency.LockFactory;
import se.tink.backend.common.concurrency.StatisticsActivitiesLock;
import se.tink.backend.common.config.StatisticConfiguration;
import se.tink.backend.common.dao.StatisticDao;
import se.tink.backend.common.exceptions.LockException;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.statistics.StatisticQueryExecutor;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StatisticQuery;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.main.TestUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ResolutionTypes;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.libraries.date.ResolutionTypes.DAILY;
import static se.tink.libraries.date.ResolutionTypes.MONTHLY;
import static se.tink.libraries.date.ResolutionTypes.MONTHLY_ADJUSTED;
import static se.tink.libraries.date.ResolutionTypes.WEEKLY;

@RunWith(JUnitParamsRunner.class)
public class StatisticsServiceControllerTest {
    private static final String USER_ID = "9a22554ae12c49e2a332bd049fa5050b";
    private static final String NOT_POPOULATED_USERID = "9a22554ae12c49e2a332bd049fa50503";
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Mock
    private StatisticDao cassandraStatisticsDao;
    @Mock
    private StatisticConfiguration statisticConfiguration;
    @Mock
    private LockFactory lockFactory;
    @Mock
    private StatisticsActivitiesLock statisticsActivitiesLock;
    @Mock
    private CredentialsRepository credentialsRepository;
    private final boolean provideYearly = true;
    private StatisticQueryExecutor statisticQueryExecutor =
            Mockito.spy(new StatisticQueryExecutor(new StatisticConfiguration(provideYearly)));
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private StatisticsServiceController statisticsServiceController;

    @Before
    public void setUp() throws LockException {
        User user = mock(User.class);
        UserProfile profile = mock(UserProfile.class);
        when(user.getProfile()).thenReturn(profile);
        when(profile.getPeriodMode()).thenReturn(MONTHLY_ADJUSTED);
        when(profile.getPeriodAdjustedDay()).thenReturn(25);

        when(userRepository.findOne(anyString())).thenReturn(user);
        when(cassandraStatisticsDao.findAllByUserIdAndPeriods(anyString(), any())).thenAnswer(invocationOnMock -> {
            String userArg = invocationOnMock.getArgument(0);
            return generateStatisticsList().stream()
                    .filter(cs -> cs.getUserId().equals(userArg)).collect(
                            Collectors.toList());
        });
        when(lockFactory.getStatisticsAndActivitiesLock(anyString())).thenReturn(statisticsActivitiesLock);
        when(statisticConfiguration.getMonthsOfStatistics()).thenReturn(12);
        when(statisticsActivitiesLock.waitForRead(anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(credentialsRepository.findAllByUserId(anyString())).thenReturn(Lists.newArrayList());
    }

    private List<Statistic> generateStatisticsList() {
        return Lists
                .newArrayList(createStatistic("2016-09-06", 100, DAILY, Statistic.Types.BALANCES_BY_ACCOUNT),
                        createStatistic("2016:18", 12345, WEEKLY, Statistic.Types.INCOME_AND_EXPENSES_AND_TRANSFERS),
                        createStatistic("2016-08", 1000, MONTHLY, Statistic.Types.EXPENSES_BY_CATEGORY),
                        createStatistic("2016-07", 0, MONTHLY_ADJUSTED, Statistic.Types.INCOME_BY_CATEGORY),
                        createStatistic("2016-02-06", 56, DAILY, Statistic.Types.EXPENSES_COUNT_BY_CATEGORY),
                        createStatistic("2016-02-06", 101, DAILY, Statistic.Types.INCOME_AND_EXPENSES_COUNT),
                        createStatistic("2014-10-06", 10001, DAILY, Statistic.Types.INCOME_NET));
    }

    @Test
    @Parameters({
            "MONTHLY, expenses-by-category",
            "MONTHLY_ADJUSTED, income-by-category"
    })
    public void listDefaultStatisticsByUserPeriodMode(ResolutionTypes periodMode, String statisticType)
            throws LockException {
        List<Statistic> statistics = statisticsServiceController.list(USER_ID, periodMode);

        assertEquals(1, statistics.size());
        assertEquals(statisticType, statistics.get(0).getType());
    }

    @Test
    public void doNotListStatisticsByIncorrectPeriodMode() throws LockException {
        List<Statistic> statistics = statisticsServiceController.list(USER_ID, DAILY);

        assertTrue(statistics.isEmpty());
    }

    @Test
    public void listStatisticsByNullPeriodMode() throws LockException {
        List<Statistic> statistics = statisticsServiceController.list(USER_ID, null);

        assertEquals(1, statistics.size());
        assertEquals(MONTHLY, statistics.get(0).getResolution());
    }

    @Test
    public void listForNewUser() throws LockException {
        List<Statistic> statistics = statisticsServiceController.list(NOT_POPOULATED_USERID, MONTHLY);

        assertTrue(statistics.isEmpty());
    }

    @Test
    @Parameters({
            "DAILY, 4",
            "WEEKLY, 1",
            "MONTHLY, 1",
            "MONTHLY_ADJUSTED, 1",
            "YEARLY, 1", // aggregated monthly statistics (yearly is enabled only for one of the two types)
            "ALL, 0"
    })
    public void filterStatisticsByResolutionType(ResolutionTypes resolution, int statisticSize) throws LockException {
        StatisticQuery statisticQuery = new StatisticQuery();
        statisticQuery.setTypes(getAllStatisticType());
        statisticQuery.setResolution(resolution);

        List<Statistic> statistics = statisticsServiceController
                .query(USER_ID, MONTHLY, statisticQuery);

        assertEquals(statisticSize, statistics.size());
        for (Statistic statistic : statistics) {
            assertEquals(resolution, statistic.getResolution());
        }
    }

    @Test
    @Parameters({
            "expenses-by-category/by-count, 1",
            "expenses-by-category, 0",
            "income-by-category, 0",
            ",0"
    })
    public void filterStatisticsByType(String type, int statisticSize) throws LockException {
        StatisticQuery statisticQuery = new StatisticQuery();
        statisticQuery.setTypes(Collections.singletonList(type));
        statisticQuery.setResolution(DAILY);

        List<Statistic> statistics = statisticsServiceController
                .query(USER_ID, MONTHLY, statisticQuery);

        assertEquals(statisticSize, statistics.size());
        for (Statistic statistic : statistics) {
            assertEquals(type, statistic.getType());
        }
    }

    @Test
    public void filterStatisticsByMultipleType() throws LockException {
        List<String> types = asList("income-and-expenses/by-count", "balances-by-account");
        StatisticQuery statisticQuery = new StatisticQuery();
        statisticQuery.setTypes(types);
        statisticQuery.setResolution(DAILY);

        List<Statistic> statistics = statisticsServiceController
                .query(USER_ID, MONTHLY, statisticQuery);

        assertEquals(2, statistics.size());

        Iterable<String> statisticTypes = getStatisticType(statistics);
        assertThat(statisticTypes).containsOnlyElementsOf(types);
    }

    @Test
    @Parameters({
            "left-to-spend, 0",
            "left, 0",
            "expenses-by-category/by-count, 1",
            "expenses-by-category, 0"
    })
    public void filterStatisticsByDescription(String description, int statisticSize) throws LockException {
        StatisticQuery statisticQuery = new StatisticQuery();
        statisticQuery.setTypes(getAllStatisticType());
        statisticQuery.setResolution(DAILY);
        statisticQuery.setDescription(description);

        List<Statistic> statistics = statisticsServiceController
                .query(USER_ID, MONTHLY, statisticQuery);

        assertEquals(statisticSize, statistics.size());
        for (Statistic statistic : statistics) {
            assertEquals(description, statistic.getDescription());
        }
    }

    @Test
    @Parameters({
            "2016-09-06, 1",
            "2016-05-06, 0",
            "2014-10-06, 1",
            "2016, 0"
    })
    public void filterStatisticsByPeriods(String period, int statisticSize) throws LockException {
        StatisticQuery statisticQuery = new StatisticQuery();
        statisticQuery.setTypes(getAllStatisticType());
        statisticQuery.setResolution(DAILY);
        statisticQuery.setPeriods(Collections.singletonList(period));

        List<Statistic> statistics = statisticsServiceController
                .query(USER_ID, MONTHLY, statisticQuery);

        assertEquals(statisticSize, statistics.size());
        for (Statistic statistic : statistics) {
            assertEquals(period, statistic.getPeriod());
        }
    }

    @Test
    public void filterStatisticsByMultiplePeriods() throws LockException {
        List<String> periods = asList("2016-09-06", "2016-02-06");
        StatisticQuery statisticQuery = new StatisticQuery();
        statisticQuery.setTypes(getAllStatisticType());
        statisticQuery.setResolution(DAILY);
        statisticQuery.setPeriods(periods);

        List<Statistic> statistics = statisticsServiceController
                .query(USER_ID, MONTHLY, statisticQuery);

        assertFalse(statistics.isEmpty());
        assertEquals(3, statistics.size());

        Iterable<String> statisticPeriods = getStatisticPeriods(statistics);
        assertThat(statisticPeriods).containsOnlyElementsOf(periods);
    }

    @Test
    public void doNotReturnStatisticsForEmptyPeriodsList() throws LockException {
        StatisticQuery statisticQuery = new StatisticQuery();
        statisticQuery.setTypes(getAllStatisticType());
        statisticQuery.setResolution(DAILY);
        statisticQuery.setPeriods(Collections.emptyList());

        List<Statistic> statistics = statisticsServiceController
                .query(USER_ID, MONTHLY, statisticQuery);

        assertTrue(statistics.isEmpty());
    }

    @Test
    public void padStatistics() throws LockException {
        StatisticQuery statisticQuery = new StatisticQuery();
        statisticQuery.setTypes(getAllStatisticType());
        statisticQuery.setResolution(MONTHLY);
        statisticQuery.setPadResultUntilToday(true);

        List<Statistic> statistics = statisticsServiceController
                .query(USER_ID, MONTHLY, statisticQuery);

        assertFalse(statistics.isEmpty());

        Iterable<String> statisticPeriods = getStatisticPeriods(statistics);

        assertThat(statisticPeriods).containsOnlyOnce("2016-08", "2016-09", DateUtils.getCurrentMonthPeriod());
        assertThat(statisticPeriods).doesNotContain("2016-07");
    }

    @Test
    public void mergeStatisticsForCoupleQueries() throws LockException {
        StatisticQuery statisticQuery1 = new StatisticQuery();
        statisticQuery1.setTypes(Collections.singletonList(Statistic.Types.EXPENSES_BY_CATEGORY));
        statisticQuery1.setResolution(MONTHLY);

        StatisticQuery statisticQuery2 = new StatisticQuery();
        statisticQuery2.setTypes(Collections.singletonList(Statistic.Types.INCOME_BY_CATEGORY));
        statisticQuery2.setResolution(MONTHLY_ADJUSTED);

        List<Statistic> statistics = statisticsServiceController
                .queries(USER_ID, MONTHLY, asList(statisticQuery1, statisticQuery2));

        assertEquals(2, statistics.size());

        Iterable<String> statisticTypes = getStatisticType(statistics);
        Iterable<ResolutionTypes> statisticResolution = getStatisticResolution(statistics);

        assertThat(statisticTypes).containsOnlyElementsOf(
                asList(Statistic.Types.INCOME_BY_CATEGORY, Statistic.Types.EXPENSES_BY_CATEGORY));
        assertThat(statisticResolution).containsOnlyElementsOf(asList(MONTHLY, MONTHLY_ADJUSTED));
    }

    @Test
    public void doNotReturnTheSameStatisticsForCoupleQueries() throws LockException {
        StatisticQuery statisticQuery1 = new StatisticQuery();
        statisticQuery1.setTypes(Collections.singletonList(Statistic.Types.EXPENSES_COUNT_BY_CATEGORY));
        statisticQuery1.setResolution(DAILY);

        StatisticQuery statisticQuery2 = new StatisticQuery();
        statisticQuery2.setTypes(getAllStatisticType());
        statisticQuery2.setResolution(DAILY);
        statisticQuery2.setPeriods(Collections.singletonList("2016-02-06"));

        StatisticQuery statisticQuery3 = new StatisticQuery();
        statisticQuery3.setTypes(Collections.singletonList(Statistic.Types.EXPENSES_BY_CATEGORY));
        statisticQuery3.setResolution(MONTHLY);

        List<Statistic> statistics = statisticsServiceController
                .queries(USER_ID, MONTHLY,
                        asList(statisticQuery1, statisticQuery2, statisticQuery3));

        assertEquals(3, statistics.size());

        Iterable<String> statisticPeriods = getStatisticPeriods(statistics);
        Iterable<String> statisticTypes = getStatisticType(statistics);

        assertThat(statisticPeriods).containsOnlyElementsOf(asList("2016-02-06", "2016-08"));
        assertThat(statisticTypes).containsOnlyElementsOf(
                asList(Statistic.Types.EXPENSES_COUNT_BY_CATEGORY, Statistic.Types.EXPENSES_BY_CATEGORY,
                        Statistic.Types.INCOME_AND_EXPENSES_COUNT));
    }

    @Test
    public void returnStatisticsFalseLockWaiting() throws LockException {
        when(statisticsActivitiesLock.waitForRead(anyLong(), any(TimeUnit.class))).thenReturn(false);

        StatisticQuery statisticQuery = new StatisticQuery();
        statisticQuery.setTypes(Collections.singletonList(Statistic.Types.EXPENSES_BY_CATEGORY));
        statisticQuery.setResolution(MONTHLY);

        List<Statistic> statistics = statisticsServiceController
                .query(USER_ID, MONTHLY, statisticQuery);
        assertFalse(statistics.isEmpty());
    }

    @Test(expected = LockException.class)
    public void throwExceptionOnExectionOnLockWaiting() throws LockException {
        when(statisticsActivitiesLock.waitForRead(anyLong(), any(TimeUnit.class))).thenThrow(new LockException());

        StatisticQuery statisticQuery = new StatisticQuery();
        statisticQuery.setTypes(Collections.singletonList(Statistic.Types.EXPENSES_BY_CATEGORY));
        statisticQuery.setResolution(MONTHLY);

        statisticsServiceController.query(USER_ID, MONTHLY, statisticQuery);

    }

    private List<String> getAllStatisticType() {
        return getStatisticType(generateStatisticsList());
    }

    private List<String> getStatisticType(List<Statistic> statistics) {
        return statistics.stream().map(Statistic::getType).collect(Collectors.toList());
    }

    private List<ResolutionTypes> getStatisticResolution(List<Statistic> statistics) {
        return statistics.stream().map(Statistic::getResolution).collect(Collectors.toList());
    }

    private List<String> getStatisticPeriods(List<Statistic> statistics) {
        return statistics.stream().map(Statistic::getPeriod).collect(Collectors.toList());
    }

    private Statistic createStatistic(String period, double value, ResolutionTypes resolution,
            String type) {
        return TestUtils
                .createStatistic(type, period, value, resolution, type, USER_ID, "Payload");
    }

}
