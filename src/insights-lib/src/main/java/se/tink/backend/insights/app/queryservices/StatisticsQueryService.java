package se.tink.backend.insights.app.queryservices;

import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;
import com.google.inject.Inject;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.joda.time.DateTime;
import se.tink.backend.common.dao.StatisticDao;
import se.tink.backend.core.Statistic;
import se.tink.backend.insights.core.domain.contents.MonthlySummaryInsightCategoryData;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.utils.ListFilterUtils;
import se.tink.backend.insights.utils.LogUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.ResolutionTypes;

public class StatisticsQueryService {

    private CategoryQueryService categoryQueryService;
    private StatisticDao statisticDao;
    private static final LogUtils log = new LogUtils(StatisticsQueryService.class);

    public static final Ordering<Statistic> STATISTICS_VALUE_ORDERING = new Ordering<Statistic>() {
        @Override
        public int compare(Statistic left, Statistic right) {
            return Doubles.compare(left.getValue(), right.getValue());
        }
    };

    @Inject
    public StatisticsQueryService(CategoryQueryService categoryQueryService,
            StatisticDao statisticDao) {
        this.categoryQueryService = categoryQueryService;
        this.statisticDao = statisticDao;
    }

    public List<Statistic> getUserStatistics(UserId userId) {
        return statisticDao.findAllByUserIdAndPeriods(userId.value(),
                getPeriods(DateTime.now().minusMonths(3), DateTime.now().plusMonths(1)));
    }


    private List<Integer> getPeriods(DateTime startDate, DateTime endDate) {
        return DateUtils
                .getYearMonthPeriods(YearMonth.of(startDate.getYear(), startDate.getMonthOfYear()),
                        YearMonth.of(endDate.getYear(), endDate.getMonthOfYear()))
                .stream()
                .sorted(Comparator.reverseOrder())
                .limit(3)
                .collect(Collectors.toList());
    }

    public ImmutableList<Statistic> filterByResolution(List<Statistic> statistics, ResolutionTypes type) {
        return filterStatistics(statistics, Statistic::getResolution, type, Object::equals);
    }

    public ImmutableList<Statistic> filterByStatisticType(List<Statistic> statistics, String type) {
        return filterStatistics(statistics, Statistic::getType, type, Object::equals);
    }

    public ImmutableList<Statistic> filterByCategoryName(List<Statistic> statistics, String name) {
        return filterStatistics(statistics, Statistic::getDescription, name, Objects::equal);
    }

    private <T> ImmutableList<Statistic> filterStatistics(List<Statistic> statistics,
            Function<Statistic, T> statisticFunction, T requirement, BiFunction<T, T, Boolean> filterFunction) {
        return ListFilterUtils.filterObjectList(statistics, statisticFunction, requirement, filterFunction);
    }

    // Todo: Refactor out stuff from here to make it smaller
    // Todo: generalize to a general insightCategoryData list.
    public List<MonthlySummaryInsightCategoryData> largestCategoryExpensesByPeriod(List<Statistic> filteredStatistics,
            Period period) {

        ImmutableListMultimap<String, Statistic> statisticsByType = Multimaps.index(
                filteredStatistics,
                Statistic::getType);

        ListMultimap<String, Statistic> interestingExpensesByPeriodStatistics = Multimaps.index(
                statisticsByType.get(Statistic.Types.EXPENSES_BY_CATEGORY),
                Statistic::getPeriod);

        ListMultimap<String, Statistic> interestingExpensesByPeriodByCountStatistics = Multimaps.index(
                statisticsByType.get(Statistic.Types.EXPENSES_COUNT_BY_CATEGORY),
                Statistic::getPeriod);

        List<Statistic> monthInterestingExpensesByCategory = interestingExpensesByPeriodStatistics.get(period
                .getName());

        List<Statistic> monthInterestingExpensesByCategoryCount = interestingExpensesByPeriodByCountStatistics
                .get(period.getName());

        Iterable<Statistic> largestCategoryExpenses = STATISTICS_VALUE_ORDERING.leastOf(
                monthInterestingExpensesByCategory.stream().filter(s -> (!Objects.equal(s.getDescription(), "")))
                        .collect(Collectors.toList()), 3);

        List<MonthlySummaryInsightCategoryData> largestCategories = Lists.newArrayList();

        LoadingCache<String, DescriptiveStatistics> descriptiveStatisticsByCategoryId = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, DescriptiveStatistics>() {
                    @Override
                    public DescriptiveStatistics load(String key) throws Exception {
                        return new DescriptiveStatistics(6);
                    }
                });

        for (Statistic s : largestCategoryExpenses) {
            double previousSpendingsSum = 0;

            double[] previousSpendings = null;
            try {
                previousSpendings = descriptiveStatisticsByCategoryId.get(
                        s.getDescription()).getValues();
                for (double previousSpending : previousSpendings) {
                    previousSpendingsSum += previousSpending;
                }
            } catch (ExecutionException e) {
                log.warn("Could not fetch values from descriptiveStatisticsByCategoryId UserID", e);
            }

            double previousSpendingsAverage = (previousSpendings == null) ?
                    previousSpendingsSum : previousSpendingsSum / previousSpendings.length;

            final MonthlySummaryInsightCategoryData categorySummaryData = new MonthlySummaryInsightCategoryData(
                    categoryQueryService.findById(s.getDescription()).getDisplayName(),
                    s.getValue(),
                    (int) monthInterestingExpensesByCategoryCount.stream().filter(s1 -> (s1.getDescription().equals(
                            s.getDescription()))).findFirst().get().getValue(),
                    previousSpendingsAverage);

            largestCategories.add(categorySummaryData);
        }

        return largestCategories;
    }



}
