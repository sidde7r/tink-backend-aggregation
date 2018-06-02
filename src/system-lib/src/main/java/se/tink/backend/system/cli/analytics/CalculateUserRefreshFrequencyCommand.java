package se.tink.backend.system.cli.analytics;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.util.concurrent.RateLimiter;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.cassandra.CredentialsEventRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsEvent;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class CalculateUserRefreshFrequencyCommand extends ServiceContextCommand<ServiceConfiguration> {
    protected static final LogUtils log = new LogUtils(CalculateUserRefreshFrequencyCommand.class);

    public CalculateUserRefreshFrequencyCommand() {
        super("calculate-user-refresh-frequency", "Calculates all user refresh frequency.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        log.info("Calculating user refresh frequency.");

        Integer threadPoolSize = Integer.getInteger("threadPoolSize", 10);
        Integer ratePerSecond = Integer.getInteger("ratePerSecond", 10);
        final String market = System.getProperty("market", "all");
        String startYearString = System.getProperty("startYear", "2014");
        String startMonthString = System.getProperty("startMonth", "9");
        String nbrOfUsers = System.getProperty("nbrOfUsers", "all");
        
        int numberOfUsers = Integer.MAX_VALUE;
        
        if (!nbrOfUsers.equals("all")) {
            numberOfUsers = Integer.parseInt(nbrOfUsers);
        }
        
        final int startYear = Integer.valueOf(startYearString);
        final int startMonth = Integer.valueOf(startMonthString);

        final RateLimiter rateLimiter = RateLimiter.create(ratePerSecond, 30, TimeUnit.SECONDS);

        final CredentialsEventRepository credentialsEventRepository = serviceContext.getRepository(CredentialsEventRepository.class);
        final CredentialsRepository credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        final AggregationControllerCommonClient aggregationControllerCommonClient = serviceContext
                .getAggregationControllerCommonClient();
        final UserRepository userRepository = serviceContext.getRepository(UserRepository.class);

        final File resultDailyFile = new File("data/test/userRefreshAnalysis_daily.txt");
        final File resultWeeklyFile = new File("data/test/userRefreshAnalysis_weekly.txt");
        final File userRankFile = new File("data/test/userRefreshRanking.txt");

        // Create headings of result file and statistics containers. 
        
        final Map<Long, Map<String, DescriptiveStatistics>> statisticsDailyResults = Maps.newHashMap();
        final Map<Long, Map<String, DescriptiveStatistics>> statisticsWeeklyResults = Maps.newHashMap();
        final Map<Long, DescriptiveStatistics> statisticsDailyUserResults = Maps.newHashMap();
        final Map<Long, DescriptiveStatistics> statisticsWeeklyUserResults = Maps.newHashMap();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, startYear);
        calendar.set(Calendar.MONTH, startMonth + 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        
        System.out.println("Start time: " + ThreadSafeDateFormat.FORMATTER_DAILY.format(calendar.getTime()));

        Files.write("rank" + "\t" + "users" + "\t", resultDailyFile, Charsets.UTF_8);
        Files.write("rank" + "\t" + "users" + "\t", resultWeeklyFile, Charsets.UTF_8);
        Files.write("userId" + "\t" + "rankDaily" + "\t" + "rankMontly" + "\n", userRankFile, Charsets.UTF_8);

        while (calendar.getTime().before(ThreadSafeDateFormat.FORMATTER_DAILY.parse("2015-04-01"))) {
            String yearWeek = String.valueOf(calendar.get(Calendar.YEAR)) + "-" + String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR));
            Files.append(yearWeek + "\t", resultDailyFile, Charsets.UTF_8);
            Files.append(yearWeek + "\t", resultWeeklyFile, Charsets.UTF_8);
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }

        Files.append("\n", resultDailyFile, Charsets.UTF_8);
        Files.append("\n", resultWeeklyFile, Charsets.UTF_8);

        // Get providers.
        final Map<String, Provider> providersByName;
        if (serviceContext.isProvidersOnAggregation()) {
            providersByName = Maps.uniqueIndex(aggregationControllerCommonClient.listProviders(), Provider::getName);
        } else {
            providersByName = Maps.uniqueIndex(serviceContext.getRepository(ProviderRepository.class).findAll(),
                    Provider::getName);
        }

        // Add all users from the users table.

        Set<String> users = Sets.newHashSet(Iterables.transform(
                Iterables.filter(userRepository.findAll(),
                        u -> Objects.equal(market, "all") || Objects.equal(u.getProfile().getMarket(), market)),
                User::getId));

        int usersCount = 0;

        log.info("Calculating user reresh frequency for " + users.size() + " users");

        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        for (final String userId : users) {
            usersCount++;

            final int finalUsersCount = usersCount;
            
            if (usersCount > numberOfUsers) {
                break;
            }

            executor.execute(() -> {
                try {
                    rateLimiter.acquire();
                    log.info("\tDoing user #" + finalUsersCount + ", with id: "
                            + userId);

                    List<Credentials> credentials = credentialsRepository.findAllByUserId(userId);
                    Iterable<CredentialsEvent> credentialsEvents = Iterables.filter(
                            Iterables.concat(Iterables.transform(credentials,
                                    c -> credentialsEventRepository
                                            .findByUserIdAndCredentialsId(c.getUserId(), c.getId()))), ae -> {
                                        if (ae.getStatus() != CredentialsStatus.UPDATED) {
                                            return false;
                                        }

                                        Provider provider = providersByName.get(ae.getProviderName());
                                        if (provider == null || !provider.isTransactional()) {
                                            return false;
                                        }
                                        return true;
                            });

                    // Find training month agent events by day and evaluate rank against comings weeks.

                    final Calendar calendar1 = Calendar.getInstance();

                    Iterable<CredentialsEvent> rankingDailyAgentEvents = Iterables.filter(credentialsEvents,
                            ae -> {
                                calendar1.setTime(ae.getTimestamp());
                                return calendar1.get(Calendar.YEAR) == startYear
                                        && calendar1.get(Calendar.MONTH) == startMonth;
                            });

                    ImmutableListMultimap<Integer, CredentialsEvent> rankingDailyAgentEventsByDay = Multimaps.index(
                            rankingDailyAgentEvents, ae -> {
                                calendar1.setTime(ae.getTimestamp());
                                return calendar1.get(Calendar.DAY_OF_MONTH);
                            });

                    // Calculate daily ranking.

                    calendar1.set(Calendar.MONTH, startMonth);
                    int daysInMonth = calendar1.getActualMaximum(Calendar.DAY_OF_MONTH);
                    int daysWithData = 0;

                    for (int day = 1; day <= daysInMonth; day++) {
                        ImmutableList<CredentialsEvent> events = rankingDailyAgentEventsByDay.get(day);
                        if (events != null && events.size() != 0) {
                            daysWithData++;
                        }
                    }

                    double rankDaily = ((double) daysWithData) / daysInMonth * 100;

                    // Find rank from weekly activity.

                    calendar1.set(Calendar.MONTH, startMonth);
                    calendar1.set(Calendar.DAY_OF_MONTH, calendar1.getActualMaximum(Calendar.DAY_OF_MONTH));

                    final Date traingingPeriodStop = calendar1.getTime();

                    calendar1.add(Calendar.WEEK_OF_YEAR, -12);

                    final Date traingingPeriodStart = calendar1.getTime();

                    Iterable<CredentialsEvent> rankingWeeklyAgentEvents = Iterables.filter(credentialsEvents,
                            ae -> ae.getTimestamp().after(traingingPeriodStart) && ae.getTimestamp()
                                    .before(traingingPeriodStop));

                    ImmutableListMultimap<Integer, CredentialsEvent> rankingWeeklyAgentEventsByDay = Multimaps.index(
                            rankingWeeklyAgentEvents, ae -> {
                                calendar1.setTime(ae.getTimestamp());
                                return calendar1.get(Calendar.WEEK_OF_YEAR);
                            });

                    // Calculate weekly ranking.

                    calendar1.set(Calendar.MONTH, startMonth);
                    calendar1.set(Calendar.DAY_OF_MONTH, calendar1.getActualMaximum(Calendar.DAY_OF_MONTH));
                    calendar1.add(Calendar.WEEK_OF_YEAR, -12);

                    int startWeek = calendar1.get(Calendar.WEEK_OF_YEAR);
                    int weeksWithData = 0;

                    for (int week = startWeek; week <= startWeek + 12; week++) {
                        ImmutableList<CredentialsEvent> events = rankingWeeklyAgentEventsByDay.get(week);
                        if (events != null && events.size() != 0) {
                            weeksWithData++;
                        }
                    }

                    double rankWeekly = ((double) weeksWithData) / 12 * 100;

                    Files.append(userId + "\t" + Math.round(rankDaily) + "\t" + Math.round(rankWeekly) + "\n",
                            userRankFile, Charsets.UTF_8);

                    // Evaluate coming months for user.

                    ImmutableListMultimap<String, CredentialsEvent> agentEventsByYearAndWeek = Multimaps.index(
                            Iterables.filter(credentialsEvents, ae -> {
                                calendar1.setTime(ae.getTimestamp());
                                return (calendar1.get(Calendar.YEAR) == startYear
                                        && calendar1.get(Calendar.MONTH) > startMonth)
                                        || (
                                        calendar1.get(Calendar.YEAR) == startYear + 1
                                                && calendar1.get(Calendar.MONTH) < 4);
                            }), ae -> {
                                calendar1.setTime(ae.getTimestamp());
                                return calendar1.get(Calendar.YEAR) + "-" + calendar1.get(Calendar.WEEK_OF_YEAR);
                            });

                    // Check if weeks have updates.

                    calendar1.set(Calendar.YEAR, startYear);
                    calendar1.set(Calendar.MONTH, startMonth + 1);
                    calendar1.set(Calendar.DAY_OF_MONTH, 1);

                    while (calendar1.getTime().before(ThreadSafeDateFormat.FORMATTER_DAILY.parse("2015-04-01"))) {
                        String yearWeek = calendar1.get(Calendar.YEAR) + "-" + calendar1.get(Calendar.WEEK_OF_YEAR);
                        ImmutableList<CredentialsEvent> events = agentEventsByYearAndWeek.get(yearWeek);

                        // Daily stats.

                        Map<String, DescriptiveStatistics> statisticsMapForRankDaily = statisticsDailyResults
                                .get(Math.round(rankDaily));

                        if (statisticsMapForRankDaily == null) {
                            statisticsMapForRankDaily = Maps.newHashMap();
                            statisticsDailyResults.put(Math.round(rankDaily), statisticsMapForRankDaily);
                        }

                        DescriptiveStatistics stats = statisticsMapForRankDaily.get(yearWeek);

                        if (stats == null) {
                            stats = new DescriptiveStatistics();
                            statisticsMapForRankDaily.put(yearWeek, stats);
                        }

                        if (events == null || events.size() == 0) {
                            stats.addValue(0);
                        } else {
                            stats.addValue(1);
                        }

                        // Weekly stats

                        Map<String, DescriptiveStatistics> statisticsMapForRankWeekly = statisticsWeeklyResults
                                .get(Math.round(rankWeekly));

                        if (statisticsMapForRankWeekly == null) {
                            statisticsMapForRankWeekly = Maps.newHashMap();
                            statisticsWeeklyResults.put(Math.round(rankWeekly), statisticsMapForRankWeekly);
                        }

                        stats = statisticsMapForRankWeekly.get(yearWeek);

                        if (stats == null) {
                            stats = new DescriptiveStatistics();
                            statisticsMapForRankWeekly.put(yearWeek, stats);
                        }

                        if (events == null || events.size() == 0) {
                            stats.addValue(0);
                        } else {
                            stats.addValue(1);
                        }

                        calendar1.add(Calendar.WEEK_OF_YEAR, 1);
                    }

                    DescriptiveStatistics usersStatsDaily = statisticsDailyUserResults.get(Math.round(rankDaily));
                    DescriptiveStatistics usersStatsWeekly = statisticsWeeklyUserResults.get(Math.round(rankWeekly));

                    if (usersStatsDaily == null) {
                        usersStatsDaily = new DescriptiveStatistics();
                        statisticsDailyUserResults.put(Math.round(rankDaily), usersStatsDaily);
                    }
                    usersStatsDaily.addValue(1);

                    if (usersStatsWeekly == null) {
                        usersStatsWeekly = new DescriptiveStatistics();
                        statisticsWeeklyUserResults.put(Math.round(rankWeekly), usersStatsWeekly);
                    }
                    usersStatsWeekly.addValue(1);

                } catch (Exception e) {
                    log.error("Failed to calculate refresh frequency for userId: " + userId, e);
                }
            });
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Could not calculate user refresh frequency", e);
        }
        
        DecimalFormat df = new DecimalFormat("#.##");
        
        // Print results for daily rank.
        
        for (Long rank : statisticsDailyResults.keySet()) {
            Map<String, DescriptiveStatistics> statisticsByRankDaily = statisticsDailyResults.get(rank);
            
            StringBuffer buffer = new StringBuffer();
            
            calendar.set(Calendar.YEAR, startYear);
            calendar.set(Calendar.MONTH, startMonth + 1);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            
            while (calendar.getTime().before(ThreadSafeDateFormat.FORMATTER_DAILY.parse("2015-04-01"))) {
                String yearWeek = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.WEEK_OF_YEAR);
                DescriptiveStatistics stats = statisticsByRankDaily.get(yearWeek);
                
                // Get average for this rank and this week.
                
                if (stats == null || stats.getN() == 0) {
                    buffer.append(0 + "\t");
                } else {
                    buffer.append(df.format(stats.getMean()) + "\t");
                }
                
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
            }
            
            Files.append(rank + "\t" + statisticsDailyUserResults.get(rank).getN() + "\t" + buffer.toString() + "\n", resultDailyFile, Charsets.UTF_8);
        }
        
        // Print results for weekly rank.

        for (Long rank : statisticsWeeklyResults.keySet()) {
            Map<String, DescriptiveStatistics> statisticsByRankWeekly = statisticsWeeklyResults.get(rank);
            
            StringBuffer buffer = new StringBuffer();
            
            calendar.set(Calendar.YEAR, startYear);
            calendar.set(Calendar.MONTH, startMonth + 1);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            
            while (calendar.getTime().before(ThreadSafeDateFormat.FORMATTER_DAILY.parse("2015-04-01"))) {
                String yearWeek = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.WEEK_OF_YEAR);
                DescriptiveStatistics stats = statisticsByRankWeekly.get(yearWeek);
                
                // Get average for this rank and this week.
                
                if (stats == null || stats.getN() == 0) {
                    buffer.append(0 + "\t");
                } else {
                    buffer.append(df.format(stats.getMean()) + "\t");
                }
                
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
            }
            
            Files.append(rank + "\t" + statisticsWeeklyUserResults.get(rank).getN() + "\t" + buffer.toString() + "\n", resultWeeklyFile, Charsets.UTF_8);
        }
        log.info("Done calculating user refresh frequency for " + usersCount + " users");

    }
}
