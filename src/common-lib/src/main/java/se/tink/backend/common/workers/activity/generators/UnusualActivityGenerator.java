package se.tink.backend.common.workers.activity.generators;

import com.google.api.client.repackaged.com.google.common.annotations.VisibleForTesting;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.Precision;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Category;
import se.tink.backend.core.KVPair;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.UnusualCategoryActivityData;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.StringUtils;

import static com.google.common.collect.Iterables.filter;

public class UnusualActivityGenerator extends ActivityGenerator {

    private static final Ordering<KVPair<String, Double>> ACTIVITY_DATA_PERIOD_ORDERING = new Ordering<KVPair<String, Double>>() {
        @Override
        public int compare(KVPair<String, Double> left, KVPair<String, Double> right) {
            return left.getKey().compareTo(right.getKey());
        }
    };
    private static final int MAX_NBR_OF_MONTHS = 9;
    private static final double MIN_DEVIATION_THRESHOLD = 25;
    private static final int MIN_NBR_OF_MONTHS = 6;
    private static final int DEFAULT_MINIMUM_TRANSACTIONS_IN_CATEGORY = 20;
    private static final int DEFAULT_MAXIMUM_NO_SPENDING_MONTHS = 2;
    private static final Ordering<Statistic> STATISTICS_PERIOD_ORDERING = new Ordering<Statistic>() {
        @Override
        public int compare(Statistic left, Statistic right) {
            return left.getPeriod().compareTo(right.getPeriod());
        }
    };
    private static final double STD_DEVIATION_HIGH_THRESHOLD = 1.25;
    private static final double STD_DEVIATION_LOW_THRESHOLD = 1.25;
    private static final double RECURRING_EXPENSES_PATTERN_VARIATION = 0.2;
    private static final int RECURRING_EXPENSES_MINIMUM_DISTANCE = 1;
    private static final int RECURRING_EXPENSES_MAXIMUM_DISTANCE = 4;
    private static final int MIN_RECURRING_EXPENSES_PATTERN_MONTHS = 3;
    static final MetricId RECURRING_EXPENSES_METRIC = MetricId.newId("recurring_expenses_pattern");

    private static double calculateRatio(UnusualCategoryActivityData existingData) {
        KVPair<String, Double> lastDataPoint = Iterables.getLast(existingData.getData());
        KVPair<String, Double> lastAverageDataPoint = Iterables.getLast(existingData.getDataAverages());

        return (lastDataPoint.getValue() / lastAverageDataPoint.getValue());
    }

    private static Date findPeriodEndDate(UserProfile profile, KVPair<String, Double> dataPoint) {
        return DateUtils.getCurrentOrPreviousBusinessDay(DateUtils.getLastDateFromPeriod(dataPoint.getKey(),
                profile.getPeriodMode(), profile.getPeriodAdjustedDay()));
    }

    private final int minimumTransactionsInCategory;
    private final int maximumNoSpendingMonths;
    private final MetricRegistry metricRegistry;

    @VisibleForTesting
    UnusualActivityGenerator(int minimumTransactionsInCategory, int maximumNoSpendingMonths,
                             MetricRegistry metricRegistry, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(UnusualActivityGenerator.class, 60, 80, deepLinkBuilderFactory);
        this.minimumTransactionsInCategory = minimumTransactionsInCategory;
        this.maximumNoSpendingMonths = maximumNoSpendingMonths;
        this.metricRegistry = metricRegistry;
    }

    public UnusualActivityGenerator(MetricRegistry metricRegistry, DeepLinkBuilderFactory deepLinkBuilderFactory) {
        this(DEFAULT_MINIMUM_TRANSACTIONS_IN_CATEGORY, DEFAULT_MAXIMUM_NO_SPENDING_MONTHS, metricRegistry,
                deepLinkBuilderFactory);
    }

    @Override
    public void generateActivity(final ActivityGeneratorContext context) {
        final UserProfile profile = context.getUser().getProfile();

        final Set<String> validCategories = context.getStatistics().stream()
                .filter(s -> s.getResolution() == profile.getPeriodMode()
                        && s.getType().equals(Statistic.Types.EXPENSES_COUNT_BY_CATEGORY)
                        && s.getValue() >= minimumTransactionsInCategory)
                .map(Statistic::getDescription)
                .collect(Collectors.toSet());

        final double minimumDeviationThreshold = context.getUserCurrency().getFactor() * MIN_DEVIATION_THRESHOLD;

        // Filter out the interesting categories.
        final Iterable<Category> categories = filter(context.getCategoriesByCodeForLocale().values(),
                c -> (!context.getCategoryConfiguration().getUnusualActivityExcludedCodes().contains(c.getCode())
                        && !c.isDefaultChild()
                        && validCategories.contains(c.getId())));

        // Filter out the interesting statistics.
        final Multimap<String, Statistic> statisticsByCategoryId = Multimaps.index(
                STATISTICS_PERIOD_ORDERING.sortedCopy(filter(context.getStatistics(),
                        s -> (s.getResolution() == profile.getPeriodMode()
                                && s.getType().equals(Statistic.Types.EXPENSES_BY_CATEGORY)))),
                Statistic::getDescription);

        // Keep track of less than usual activities so we only generate one per period.
        final Map<String, Activity> lessThanUsualActivitiesByPeriod = Maps.newHashMap();

        for (final Category category : categories) {
            final Collection<Statistic> categoryStatistics = statisticsByCategoryId.get(category.getId());

            if (categoryStatistics.isEmpty()) {
                continue;
            }

            final List<KVPair<String, Double>> categoryDataPoints = zeroFillActivityDataPoints(Lists.newArrayList(Iterables
                    .transform(categoryStatistics, s -> new KVPair<>(s.getPeriod(), s.getValue()))), ResolutionTypes.MONTHLY);

            // Use a rolling MAX_NBR_OF_MONTHS months window for standard deviation calculations.

            final DescriptiveStatistics rollingStatistics = new DescriptiveStatistics(MAX_NBR_OF_MONTHS);

            // Rolling MAX_NBR_OF_MONTHS months window for data we bundle in the activity data.

            final CircularFifoQueue<KVPair<String, Double>> rollingDataPoints = new CircularFifoQueue<>(MAX_NBR_OF_MONTHS);
            final CircularFifoQueue<KVPair<String, Double>> rollingDataPointAverages = new CircularFifoQueue<>(
                    MAX_NBR_OF_MONTHS);

            for (KVPair<String, Double> dataPoint : categoryDataPoints) {
                rollingDataPoints.add(dataPoint);
                rollingStatistics.addValue(dataPoint.getValue());

                rollingDataPointAverages.add(new KVPair<>(dataPoint.getKey(), rollingStatistics.getMean()));

                if (rollingStatistics.getN() < MIN_NBR_OF_MONTHS) {
                    continue;
                }

                // Calculate the thresholds.

                final double mean = rollingStatistics.getMean();
                final double std = rollingStatistics.getStandardDeviation();

                final double highThreshold = (mean - Math.max(STD_DEVIATION_HIGH_THRESHOLD * std, minimumDeviationThreshold));
                final double lowThreshold = (mean + Math.max(STD_DEVIATION_LOW_THRESHOLD * std, minimumDeviationThreshold));

                final double value = dataPoint.getValue();

                thresholdsLoop:
                for (double threshold : new double[]{lowThreshold, highThreshold}) {
                    boolean lessThanUsual = threshold == lowThreshold;

                    // Less then usual

                    if (lessThanUsual && value <= threshold) {
                        continue;
                    }

                    // More than usual

                    if (!lessThanUsual && value >= threshold) {
                        continue;
                    }

                    // Less than usual cannot be this period

                    if (lessThanUsual
                            && dataPoint.getKey().equals(
                            DateUtils.getCurrentMonthPeriod(profile.getPeriodMode(), profile.getPeriodAdjustedDay()))) {
                        continue;
                    }

                    String type;
                    String title;
                    String message;

                    if (lessThanUsual) {
                        type = Activity.Types.UNUSUAL_CATEGORY_LOW;
                        title = context.getCatalog().getString("Less than usual");
                        message = context.getCatalog().getString("You have spent less than usual on {0} this month.");
                    } else {
                        type = Activity.Types.UNUSUAL_CATEGORY_HIGH;
                        title = context.getCatalog().getString("More than usual");
                        message = context.getCatalog().getString("You have spent more than usual on {0} this month.");
                    }

                    // Get past months of data.

                    List<KVPair<String, Double>> pastDataPoints = Lists.newArrayList(rollingDataPoints);

                    // If we should warn on this data point, check there is spending in 4 of last 6 periods for more
                    // than usual, and 6 of 6 for less then usual.

                    Iterable<KVPair<String, Double>> lastSixDataPoints = ACTIVITY_DATA_PERIOD_ORDERING.greatestOf(
                            pastDataPoints, 7);

                    int noSpendingCount = 0;
                    for (KVPair<String, Double> pastDataPoint : lastSixDataPoints) {
                        if (pastDataPoint.getKey().equals(dataPoint.getKey())) {
                            continue;
                        }
                        if (pastDataPoint.getValue() == 0) {
                            noSpendingCount++;

                            if (noSpendingCount > maximumNoSpendingMonths) {
                                continue thresholdsLoop;
                            }
                        }
                    }

                    if (!lessThanUsual) {
                        if (currentMonthFollowsRecurringPattern(rollingStatistics,
                                RECURRING_EXPENSES_PATTERN_VARIATION, RECURRING_EXPENSES_MINIMUM_DISTANCE,
                                RECURRING_EXPENSES_MAXIMUM_DISTANCE, MIN_RECURRING_EXPENSES_PATTERN_MONTHS)) {
                            metricRegistry.meter(RECURRING_EXPENSES_METRIC.label("detected", "yes")).inc();
                            continue;
                        } else {
                            metricRegistry.meter(RECURRING_EXPENSES_METRIC.label("detected", "no")).inc();
                        }
                    }

                    List<KVPair<String, Double>> pastDataPointAverages = Lists.newArrayList(rollingDataPointAverages);

                    // Figure out what transaction and date triggered the signal (only used for setting the date on the
                    // activity).

                    Date triggerDate = findPeriodEndDate(profile, dataPoint);


                    // Transform it to a suitable data-structure.

                    UnusualCategoryActivityData data = new UnusualCategoryActivityData();

                    data.setPeriod(dataPoint.getKey());
                    data.setCategoryId(category.getId());
                    data.setData(pastDataPoints);
                    data.setDataAverages(pastDataPointAverages);

                    String key = String.format("%s.%s.%s", type, dataPoint.getKey(), category.getId());

                    String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);

                    Activity activity = createActivity(context.getUser().getId(), triggerDate, type, title,
                            Catalog.format(message, category.getDisplayName()), data, key, feedActivityIdentifier);

                    if (lessThanUsual) {
                        // If less than usual, we only provide one per period and need to check and see which one is
                        // more important by looking at the value-to-MA ratio.

                        String period = Iterables.getLast(pastDataPoints).getKey();

                        Activity existingActivity = lessThanUsualActivitiesByPeriod.get(period);

                        if (existingActivity != null) {
                            // We already have an activity for the period, check and see what to do.

                            UnusualCategoryActivityData existingData = (UnusualCategoryActivityData) existingActivity
                                    .getContent();

                            double existingRatio = calculateRatio(existingData);
                            double newRatio = calculateRatio(data);

                            // Replace the existing one if the new one is more important.

                            if (newRatio < existingRatio) {
                                lessThanUsualActivitiesByPeriod.put(period, activity);
                            }
                        } else {
                            // No existing activity, just add the activity.

                            lessThanUsualActivitiesByPeriod.put(period, activity);
                        }
                    } else {
                        // If more than usual, just add the activity.
                        context.addActivity(activity);
                    }
                }
            }
        }

        if (!lessThanUsualActivitiesByPeriod.isEmpty()) {
            for (Activity activity : lessThanUsualActivitiesByPeriod.values()) {
                context.addActivity(activity);
            }
        }
    }

    /**
     * Detects if current month expense (last value of {@code pastMonthsStatistics}) follows a recurring pattern.
     *
     * @param pastMonthsStatistics monthly expenses statistics; current month is the last one
     * @param variation            maximum relative difference in expenses that belong to the same pattern
     * @param minimumDistance      minimum distance between recurring expenses in months
     * @param maximumDistance      maximum distance between recurring expenses in months
     * @param minPatternMonths     minimum number of months with similar expenses to be a pattern
     * @return {@code true} if the current month's expense follows a recurring pattern
     */
    @VisibleForTesting
    static boolean currentMonthFollowsRecurringPattern(DescriptiveStatistics pastMonthsStatistics, double variation,
                                                       int minimumDistance, int maximumDistance, int minPatternMonths) {
        int monthNumber = (int) pastMonthsStatistics.getN();
        for (int expenseDistance = minimumDistance; expenseDistance <= maximumDistance; expenseDistance++) {
            int monthsToAnalyse = 1 + (minPatternMonths - 1) * expenseDistance;
            if (monthsToAnalyse > monthNumber) {
                continue;
            }
            if (similarExpensesExistInPast(pastMonthsStatistics, monthsToAnalyse, expenseDistance, variation)) {
                return true;
            }
        }
        return false;
    }

    private static boolean similarExpensesExistInPast(DescriptiveStatistics pastMonthsStatistics, int monthsToAnalyse,
                                                      int expenseDistance, double allowedRelativeVariation) {
        for (int monthsAgo = 1 + expenseDistance; monthsAgo <= monthsToAnalyse; monthsAgo += expenseDistance) {
            int monthNumber = (int) pastMonthsStatistics.getN();
            double currentMonthExpenses = pastMonthsStatistics.getElement(monthNumber - 1);
            if (!Precision.equalsWithRelativeTolerance(
                    pastMonthsStatistics.getElement(monthNumber - monthsAgo),
                    currentMonthExpenses,
                    allowedRelativeVariation)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isNotifiable() {
        return true;
    }
}
