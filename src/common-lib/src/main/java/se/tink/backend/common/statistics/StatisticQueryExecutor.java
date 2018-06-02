package se.tink.backend.common.statistics;

import com.google.api.client.util.Maps;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import se.tink.backend.common.config.StatisticConfiguration;
import se.tink.backend.common.statistics.functions.AccountBalanceToAccountTypeFunction;
import se.tink.backend.common.utils.DataUtils;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StatisticQuery;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ResolutionTypes;

public class StatisticQueryExecutor {
    private static final Ordering<String> STRING_ORDERING = Ordering.natural();
    private static final ImmutableList<String> DEFAULT_STATISTIC_TYPES = ImmutableList.of(
            Statistic.Types.EXPENSES_BY_CATEGORY, Statistic.Types.INCOME_BY_CATEGORY,
            Statistic.Types.LEFT_TO_SPEND, Statistic.Types.LEFT_TO_SPEND_AVERAGE);
    private final StatisticConfiguration statisticConfiguration;

    @Inject
    public StatisticQueryExecutor(StatisticConfiguration statisticConfiguration) {
        this.statisticConfiguration = statisticConfiguration;
    }

    public List<Statistic> queryContextStatistic(ResolutionTypes userPeriodMode, List<Statistic> statistics,
            boolean isV1Api) {
        return queryStatistics(userPeriodMode, statistics, getContextQueries(userPeriodMode, isV1Api));
    }

    public List<Statistic> queryStatistics(ResolutionTypes userPeriodMode, List<Statistic> statistics,
            final List<StatisticQuery> statisticQueries) {
        Set<Statistic> mergedStatistics = Sets.newHashSet();

        for (StatisticQuery query : statisticQueries) {
            mergedStatistics.addAll(queryStatistics(userPeriodMode, statistics, query));
        }

        return ImmutableList.copyOf(mergedStatistics);
    }

    public List<Statistic> queryStatistics(ResolutionTypes userPeriodMode, List<Statistic> statistics,
            final StatisticQuery statisticQuery) {
        if (statistics == null || Iterables.isEmpty(statistics)) {
            return ImmutableList.of();
        }

        supplementQuery(statisticQuery, userPeriodMode);

        if (!isQueryConsiderable(statisticQuery)) {
            return ImmutableList.of();
        }

        if (statisticConfiguration.provideYearly() && ResolutionTypes.YEARLY.equals(statisticQuery.getResolution())) {
            statistics.addAll(YearlyStatisticAggregator.transformMonthlyToYearly(statistics));
        }
        statistics = filterByQuery(statistics, statisticQuery);

        // Pad result with the last know value up to today
        if (statisticQuery.getPadResultUntilToday()) {
            statistics.addAll(getPaddedStatisticsUntilToday(statistics));
        }

        // Return our statistics.
        return ImmutableList.copyOf(statistics);
    }

    private List<StatisticQuery> getContextQueries(ResolutionTypes userPeriodMode, boolean isV1Api) {
        List<StatisticQuery> queries = Lists.newArrayList();

        // Income and expenses statistics.
        queries.add(new StatisticQuery()); // default query

        // Last 30 days of balance statistics with DAILY resolution.
        Date now = new Date();
        Date thirtyDaysFromToday = DateUtils.addDays(now, -30);

        StatisticQuery statisticQuery = new StatisticQuery();
        statisticQuery.setResolution(ResolutionTypes.DAILY);
        statisticQuery.setTypes(Collections.singletonList(Statistic.Types.BALANCES_BY_ACCOUNT));
        statisticQuery.setPeriods(DateUtils.createDailyPeriodList(thirtyDaysFromToday, now));
        statisticQuery.setPadResultUntilToday(true);
        queries.add(statisticQuery);

        // Balance by account `cards-and-accounts` statistics with DAILY resolution.
        statisticQuery = new StatisticQuery();
        statisticQuery.setResolution(ResolutionTypes.DAILY);
        statisticQuery.setTypes(Collections.singletonList(Statistic.Types.BALANCES_BY_ACCOUNT_TYPE_GROUP));
        statisticQuery.setPadResultUntilToday(true);
        statisticQuery.setDescription(AccountBalanceToAccountTypeFunction.CARDS_AND_ACCOUNTS);
        queries.add(statisticQuery);

        // Balance by account `loan` statistics with WEEKLY resolution for api v1 or DAILY resolution for others.
        statisticQuery = new StatisticQuery();
        statisticQuery.setResolution(isV1Api ? ResolutionTypes.WEEKLY : ResolutionTypes.DAILY);
        statisticQuery.setTypes(Collections.singletonList(Statistic.Types.BALANCES_BY_ACCOUNT_TYPE_GROUP));
        statisticQuery.setPadResultUntilToday(true);
        statisticQuery.setDescription(AccountBalanceToAccountTypeFunction.LOANS);
        queries.add(statisticQuery);

        // Balance by account `saving` statistics with WEEKLY resolution for api v1 or DAILY resolution for others.
        statisticQuery = new StatisticQuery();
        statisticQuery.setResolution(isV1Api ? ResolutionTypes.WEEKLY : ResolutionTypes.DAILY);
        statisticQuery.setTypes(Collections.singletonList(Statistic.Types.BALANCES_BY_ACCOUNT_TYPE_GROUP));
        statisticQuery.setPadResultUntilToday(true);
        statisticQuery.setDescription(AccountBalanceToAccountTypeFunction.SAVINGS);
        queries.add(statisticQuery);

        // Below queries are not needed for API v1.
        if (!isV1Api) {
            // Loan rates and balance with either MONTHLY or MONTHLY_ADJUSTED resolution.
            statisticQuery = new StatisticQuery();
            statisticQuery.setResolution(userPeriodMode);
            statisticQuery.setTypes(Lists.newArrayList(Statistic.Types.LOAN_BALANCES_BY_PROPERTY,
                    Statistic.Types.LOAN_RATES_BY_PROPERTY));
            queries.add(statisticQuery);
        }

        return queries;
    }

    private List<Statistic> getPaddedStatisticsUntilToday(Iterable<Statistic> statistics) {
        Map<String, Statistic> map = Maps.newHashMap();

        // Create a map with the newest statistic for each description and type
        for (Statistic statistic : statistics) {

            // Description is account id
            String key = statistic.getDescription() + statistic.getType() + statistic.getResolution();

            if (!map.containsKey(key)) {
                map.put(key, statistic);
            } else {
                Statistic bestStatistics = map.get(key);

                // Switch result since the new one is newer

                if (STRING_ORDERING.compare(statistic.getPeriod(), bestStatistics.getPeriod()) > 0) {
                    map.put(key, statistic);
                }
            }
        }

        List<Statistic> result = Lists.newArrayList();

        for (Statistic statistic : map.values()) {
            result.addAll(DataUtils.flatFillUntilToday(statistic, statistic.getResolution()));
        }

        return result;

    }

    private List<Statistic> filterByQuery(List<Statistic> statistics, final StatisticQuery statisticQuery) {
        return Lists.newArrayList(Iterables.filter(statistics, s -> {
            if (statisticQuery.getPeriods() != null && !statisticQuery.getPeriods().contains(s.getPeriod())) {
                return false;
            }

            if (!Strings.isNullOrEmpty(statisticQuery.getDescription())
                    && !Objects.equal(statisticQuery.getDescription(), s.getDescription())) {
                return false;
            }

            if (!statisticQuery.getTypes().contains(s.getType())) {
                return false;
            }

            return (Objects.equal(statisticQuery.getResolution(), s.getResolution()));
        }));
    }

    private boolean isQueryConsiderable(StatisticQuery query) {
        if (query.getPeriods() != null && query.getPeriods().isEmpty()) {
            return false;
        }

        if (query.getTypes().isEmpty()) {
            return false;
        }

        return !Objects.equal(query.getResolution(), ResolutionTypes.ALL);
    }

    private void supplementQuery(StatisticQuery statisticQuery, ResolutionTypes userPeriodMode) {
        if (statisticQuery.getResolution() == null) {
            statisticQuery.setResolution(userPeriodMode);
        }

        if (statisticQuery.getTypes() == null || Iterables.isEmpty(statisticQuery.getTypes())) {
            statisticQuery.setTypes(DEFAULT_STATISTIC_TYPES);
        }

        if (statisticQuery.getResolution() == null) {
            statisticQuery.setResolution(ResolutionTypes.MONTHLY);
        }
    }
}
