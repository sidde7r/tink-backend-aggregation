package se.tink.backend.main.controllers;

import com.google.inject.Inject;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.joda.time.DateTime;

import se.tink.backend.common.concurrency.LockFactory;
import se.tink.backend.common.concurrency.StatisticsActivitiesLock;
import se.tink.backend.common.config.StatisticConfiguration;
import se.tink.backend.common.dao.StatisticDao;
import se.tink.backend.common.exceptions.LockException;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.statistics.StatisticQueryExecutor;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.core.StatisticQuery;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.UserState;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.rpc.GenerateStatisticsAndActivitiesRequest;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ResolutionTypes;

public class StatisticsServiceController {
    private static final ZoneId CET = ZoneId.of("CET");
    private static final int STATISTICS_WAIT_FOR_READ_TIME_SEC = 25;
    private static final LogUtils log = new LogUtils(StatisticsServiceController.class);

    private final StatisticConfiguration statisticConfiguration;
    private final StatisticDao statisticDao;
    private final LockFactory lockFactory;
    private final SystemServiceFactory systemServiceFactory;
    private final CredentialsRepository credentialsRepository;
    private final UserRepository userRepository;
    private final UserStateRepository userStateRepository;
    private final StatisticQueryExecutor statisticQueryExecutor;

    @Inject
    public StatisticsServiceController(StatisticConfiguration statisticConfiguration, LockFactory lockFactory,
            SystemServiceFactory systemServiceFactory, CredentialsRepository credentialsRepository,
            UserRepository userRepository, UserStateRepository userStateRepository,
            StatisticQueryExecutor statisticQueryExecutor, StatisticDao statisticDao) {
        this.statisticConfiguration = statisticConfiguration;
        this.lockFactory = lockFactory;
        this.systemServiceFactory = systemServiceFactory;
        this.credentialsRepository = credentialsRepository;
        this.userRepository = userRepository;
        this.userStateRepository = userStateRepository;
        this.statisticQueryExecutor = statisticQueryExecutor;
        this.statisticDao = statisticDao;
    }

    public List<Statistic> list(String userId, ResolutionTypes userPeriodMode)
            throws LockException {
        return statisticQueryExecutor.queryStatistics(userPeriodMode, getUserStatistics(userId), new StatisticQuery());
    }

    public List<Statistic> query(String userId, ResolutionTypes userPeriodMode, StatisticQuery query)
            throws LockException {
        return statisticQueryExecutor.queryStatistics(userPeriodMode, getUserStatistics(userId), query);
    }

    public List<Statistic> queries(String userId, ResolutionTypes userPeriodMode, List<StatisticQuery> queries)
            throws LockException {
        return statisticQueryExecutor.queryStatistics(userPeriodMode, getUserStatistics(userId), queries);
    }

    public List<Statistic> getContextStatistics(String userId, ResolutionTypes userPeriodMode, boolean isV1Api)
            throws LockException {
        return statisticQueryExecutor.queryContextStatistic(userPeriodMode, getUserStatistics(userId), isV1Api);
    }

    private List<Statistic> getUserStatistics(String userId) throws LockException {
        // Wait for any pending statistics calculation.
        waitForFreshStatistics(userId);
        DateTime startDate = DateTime.now().minusMonths(statisticConfiguration.getMonthsOfStatistics() + 1);
        DateTime endDate = DateTime.now().plusMonths(1);
        List<Integer> periods = getPeriods(startDate, endDate, userId);
        List<Statistic> statistics = statisticDao.findAllByUserIdAndPeriods(userId, periods);

        if (statistics.isEmpty()) {
            statistics = reGenerateStatisticsIfFeasible(userId, statistics);
        }

        return statistics;
    }

    private List<Integer> getPeriods(DateTime startDate, DateTime endDate, String userId) {
        UserProfile userProfile  = userRepository.findOne(userId).getProfile();
        if (userProfile.getPeriodMode().equals(ResolutionTypes.MONTHLY_ADJUSTED)) {
            int periodAdjustedDay = userProfile.getPeriodAdjustedDay();
            Date currentTime = Date.from(LocalDate.now().atStartOfDay(CET).toInstant());
            LocalDate periodDayDate = DateUtils.getPeriodDate(currentTime, periodAdjustedDay).withDayOfMonth(periodAdjustedDay);
            LocalDate periodEndDate = endDate.toDate().toInstant().atZone(CET).toLocalDate();
            if (periodAdjustedDay == 0 || DateUtils.getYearMonth(periodDayDate) < DateUtils.getYearMonth(periodEndDate)) {
                endDate = endDate.minusMonths(1);
            }
        } else {
            endDate = endDate.minusMonths(1);
        }

        return DateUtils
                .getYearMonthPeriods(YearMonth.of(startDate.getYear(), startDate.getMonthOfYear()),
                        YearMonth.of(endDate.getYear(), endDate.getMonthOfYear()))
                .stream()
                .sorted(Comparator.reverseOrder())
                .limit(statisticConfiguration.getMonthsOfStatistics())
                .collect(Collectors.toList());
    }

    /**
     * If the user has credentials with UPDATED status and statistics has not been generated after the the latest
     * updated credentials was updated, generate it again.
     */
    private List<Statistic> reGenerateStatisticsIfFeasible(String userId, List<Statistic> statistics) {
        List<Credentials> credentialsList = credentialsRepository.findAllByUserId(userId);

        if (credentialsList == null) {
            return statistics;
        }

        return credentialsList.stream()
                .filter(c -> c.getUpdated() != null)
                .max(Comparator
                        .comparing(Credentials::getUpdated, Comparator.nullsLast(Comparator.naturalOrder())))
                .filter(credentials -> {
                    UserState userState = userStateRepository.findOneByUserId(userId);
                    if (userState == null) {
                        return false;
                    }
                    return userState.getStatisticsTimestamp() < credentials.getUpdated().getTime();
                }).map((credentials) -> {
                    // Request new statistics and return it.
                    log.info(userId, "Credentials updated but no statistics found, re-generating.");

                    GenerateStatisticsAndActivitiesRequest request = new GenerateStatisticsAndActivitiesRequest();
                    request.setUserId(userId);
                    request.setMode(StatisticMode.FULL);
                    request.setUserTriggered(true);

                    systemServiceFactory.getProcessService().generateStatisticsAndActivitySynchronous(request);
                    DateTime startDate = DateTime.now().minusMonths(statisticConfiguration.getMonthsOfStatistics());
                    DateTime endDate = DateTime.now().plusMonths(1);
                    List<Integer> periods = getPeriods(startDate, endDate, userId);
                    return statisticDao.findAllByUserIdAndPeriods(userId, periods);

                })
                .orElse(statistics);
    }

    private void waitForFreshStatistics(String userId) throws LockException {
        StatisticsActivitiesLock lock = lockFactory.getStatisticsAndActivitiesLock(userId);

        try {
            long timestamp = System.currentTimeMillis();

            if (!lock.waitForRead(STATISTICS_WAIT_FOR_READ_TIME_SEC, TimeUnit.SECONDS)) {
                log.info(userId, "Timeout while waiting for generating statistics");
            }

            long wait = System.currentTimeMillis() - timestamp;

            if (wait > 50) {
                log.warn("Waited for fresh statistics for " + (wait) + "ms");
            }
        } catch (Exception e) {
            log.error(userId, "Could not wait for fresh statistics", e);
            throw e;
        }
    }
}
