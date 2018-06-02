package se.tink.backend.common.workers.activity.generators;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.pojava.datetime.DateTime;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.FollowUtils;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.common.workers.activity.generators.models.FollowActivityFeedbackData;
import se.tink.backend.common.workers.activity.generators.models.MonthlySummaryActivityData;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.KVPair;
import se.tink.backend.core.MonthlySummaryActivityCategoryData;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.follow.FollowTypes;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.i18n.Catalog;

public class MonthlySummaryActivityGenerator extends ActivityGenerator {
    private static final Ordering<Transaction> TRANSACTIONS_ORDERING = new Ordering<Transaction>() {
        @Override
        public int compare(Transaction left, Transaction right) {
            return Doubles.compare(Math.abs(right.getAmount()),
                    Math.abs(left.getAmount()));
        }
    };

    private static final Ordering<Statistic> STATISTICS_VALUE_ORDERING = new Ordering<Statistic>() {
        @Override
        public int compare(Statistic left, Statistic right) {
            return Doubles.compare(left.getValue(), right.getValue());
        }
    };

    private static final Ordering<KVPair<Statistic, Double>> STATISTICS_AVG_ORDERING = new Ordering<KVPair<Statistic, Double>>() {

        @Override
        public int compare(KVPair<Statistic, Double> left, KVPair<Statistic, Double> right) {
            return Doubles.compare(Math.abs(right.getKey().getValue() / right.getValue()),
                    Math.abs(left.getKey().getValue() / left.getValue()));
        }
    };

    private static final Ordering<Period> PERIODS_ORDERING = new Ordering<Period>() {
        @Override
        public int compare(Period p1, Period p2) {
            return p1.getName().compareTo(p2.getName());
        }
    };
    
    private static final Joiner FOLLOW_ITEM_KEY_JOINER = Joiner.on(';');
    private static final int PERIOD_COUNT_FOR_AVERAGE_CALCULATION = 6; // number of periods, on which base average calculation

    private static Iterable<Category> filterUninterestingCategories(List<Category> categories,
            final Set<String> unInterestingCategoryCodes) {
        return Iterables.filter(categories, c -> unInterestingCategoryCodes.contains(c.getCode()));
    }

    private static Iterable<Transaction> filterPeriodTransactionsWithInterestingCatergories(
            Iterable<Transaction> transactions, ActivityGeneratorContext context, String period,
            ResolutionTypes periodMode, int periodAdjustedDay) {
        final Date periodStartDate = DateUtils.getCurrentOrPreviousBusinessDay(DateUtils.getFirstDateFromPeriod(period,
                periodMode, periodAdjustedDay));

        final Date periodEndDate = DateUtils.getCurrentOrPreviousBusinessDay(DateUtils.getLastDateFromPeriod(period,
                periodMode, periodAdjustedDay));

        final Iterable<Category> unInterestingCategories = filterUninterestingCategories(context.getCategories(),
                context.getCategoryConfiguration().getMonthlySummaryActivityExcludedCodes());
        final List<String> unInterestingCategoryIds = Lists.newArrayList();
        for (Category c : unInterestingCategories) {
            unInterestingCategoryIds.add(c.getId());
        }

        return Iterables.filter(transactions, t -> (t.getDate().getTime() >= periodStartDate.getTime()
                && t.getDate().getTime() <= periodEndDate.getTime()
                && !unInterestingCategoryIds.contains(t.getCategoryId())));
    }

    private static String buildBudgetSentiment(double ratio, ActivityGeneratorContext context) {
        if (ratio > 0.80) {
            return context.getCatalog().getString("Great job!");
        } else if (ratio > 0.5) {
            return context.getCatalog().getString("Good job!");
        } else {
            return context.getCatalog().getString("Come on!");
        }
    }

    private static final LogUtils log = new LogUtils(MonthlySummaryActivityGenerator.class);

    public MonthlySummaryActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(MonthlySummaryActivityGenerator.class, 90, 90, deepLinkBuilderFactory);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void generateActivity(final ActivityGeneratorContext context) {

        final ResolutionTypes periodMode = context.getUser().getProfile().getPeriodMode();

        // Statistics of the period mode of the user.
        ImmutableList<Statistic> statistics = ImmutableList.copyOf(Iterables.filter(context.getStatistics(),
                s -> Objects.equal(s.getResolution(), periodMode)));

        // Follow items of `EXPENSES` and `SEARCH` type.
        ImmutableList<FollowItem> followItems = ImmutableList.copyOf(Iterables.filter(context.getFollowItems(),
                f -> (Objects.equal(f.getType(), FollowTypes.EXPENSES) || Objects.equal(f.getType(),
                        FollowTypes.SEARCH)) && f.getFollowCriteria().getTargetAmount() != null));

        Iterable<Transaction> expenses = Iterables.filter(context.getTransactions(),
                t -> Objects.equal(t.getCategoryType(), CategoryTypes.EXPENSES));

        Iterable<Transaction> incomeTransactions = Iterables.filter(context.getTransactions(),
                t -> Objects.equal(t.getCategoryType(), CategoryTypes.INCOME));

        ImmutableMap<String, Statistic> incomeByPeriod = Maps.uniqueIndex(
                Iterables.filter(statistics, s -> Objects.equal(s.getType(), Statistic.Types.INCOME_AND_EXPENSES)
                        && Objects.equal(s.getDescription(), CategoryTypes.INCOME.name())),
                Statistic::getPeriod);

        ImmutableMap<String, Statistic> expensesByPeriod = Maps.uniqueIndex(
                Iterables.filter(statistics, s -> Objects.equal(s.getType(), Statistic.Types.INCOME_AND_EXPENSES)
                        && Objects.equal(s.getDescription(), CategoryTypes.EXPENSES.name())),
                Statistic::getPeriod);

        // Get statistics by period

        ImmutableListMultimap<String, Statistic> leftToSpendStatisticsByPeriod = Multimaps.index(
                Iterables.filter(statistics, s -> Objects.equal(s.getType(), Statistic.Types.LEFT_TO_SPEND)),
                Statistic::getPeriod);

        ImmutableListMultimap<String, Statistic> leftToSpendAverageStatisticsByPeriod = Multimaps.index(
                Iterables.filter(statistics, s -> Objects.equal(s.getType(), Statistic.Types.LEFT_TO_SPEND_AVERAGE)),
                Statistic::getPeriod);

        ThreadSafeDateFormat monthNameFormat = ThreadSafeDateFormat.FORMATTER_MONTH_NAME.toBuilder()
                .setLocale(context.getLocale()).build();

        LoadingCache<String, DescriptiveStatistics> descriptiveStatisticsByCategoryId = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, DescriptiveStatistics>() {
                    @Override
                    public DescriptiveStatistics load(String key) throws Exception {
                        return new DescriptiveStatistics(6);
                    }
                });

        Date today = new Date();
        final ImmutableList<Period> sortedPeriods = PERIODS_ORDERING.immutableSortedCopy(context.getUserState().getPeriods());
        final int periodAdjustedDay = context.getUser().getProfile().getPeriodAdjustedDay();

        for (int periodIndex = 0; periodIndex < sortedPeriods.size(); periodIndex++) {
            Period period = sortedPeriods.get(periodIndex);
            if (!period.isClean()) {
                continue;
            }

            Date periodEndDate = period.getEndDate();

            // If period end date is in the future, the month isn't over yet.
            if (periodEndDate == null || periodEndDate.after(today)) {
                continue;
            }

            String monthName = monthNameFormat.format(DateTime.parse(period.getName()).toDate());

            Statistic periodIncomeStatistic = incomeByPeriod.get(period.getName());
            Statistic periodExpensesStatistic = expensesByPeriod.get(period.getName());

            double periodIncome = (periodIncomeStatistic != null) ? periodIncomeStatistic.getValue() : 0;
            double periodExpenses = (periodExpensesStatistic != null) ? periodExpensesStatistic.getValue() : 0;

            // Don't generate activity if we haven't any income or expenses in this period.
            if (periodIncome == 0 && periodExpenses == 0) {
                continue;
            }

            double periodExpensesAvg = 0;
            if (periodExpenses != 0) {
                periodExpensesAvg = getPeriodExpensesAvg(expensesByPeriod, sortedPeriods, periodIndex);
            }

            double periodNet = periodIncome + periodExpenses;

            List<Transaction> periodLargestExpenses = Lists.newArrayList(TRANSACTIONS_ORDERING.leastOf(
                    filterPeriodTransactionsWithInterestingCatergories(expenses, context, period.getName(), periodMode,
                            periodAdjustedDay), 3));

            List<Transaction> periodLargestIncome = Lists.newArrayList(TRANSACTIONS_ORDERING.leastOf(
                    filterPeriodTransactions(incomeTransactions, period.getName(), periodMode, periodAdjustedDay), 1));

            MonthlySummaryActivityData summaryData = new MonthlySummaryActivityData();

            // Construct the follow feedback.

            if (!context.getFollowItems().isEmpty()) {
                List<FollowItem> periodFollowItems = FollowUtils.cloneFollowItems(followItems);

                FollowUtils.populateFollowItems(
                        periodFollowItems,
                        period.getName(),
                        period.getName(),
                        periodEndDate,
                        false, // Include historical amounts. Only used by `FollowTypes.SAVINGS`, which is not included.
                        false, // Include transactions
                        false, // Suggest
                        context.getUser(),
                        context.getTransactions(),
                        context.getTransactionsBySearchFollowItemId(),
                        context.getAccounts(),
                        null, // Only used by `FollowTypes.SAVINGS`, which is not included.
                        context.getCategories(),
                        context.getCategoryConfiguration());

                int numberOfFollowItems = periodFollowItems.size();

                // Check status on follow items and create the follow summary
                // object.

                FollowActivityFeedbackData followFeedback = new FollowActivityFeedbackData();

                if (numberOfFollowItems > 0) {
                    Iterable<FollowItem> positiveFollowItems = Iterables.filter(periodFollowItems,
                            Predicates.FOLLOW_ITEMS_WITH_POSITIVE_PROGRESS);

                    int numberOfPositiveFollowItems = Iterables.size(positiveFollowItems);

                    double ratioOfPassedBudgets = (double) numberOfPositiveFollowItems / (double) numberOfFollowItems;

                    if (numberOfPositiveFollowItems == numberOfFollowItems) {
                        followFeedback
                                .setFeedbackTitle(Catalog
                                        .format(context
                                                .getCatalog()
                                                .getPluralString(
                                                        "You kept your goal in {0}. Great job!",
                                                        "You kept all your goals in {0}. Great job!",
                                                        numberOfFollowItems),
                                                monthName));
                    } else if (numberOfPositiveFollowItems == 0) {
                        followFeedback
                                .setFeedbackTitle(Catalog
                                        .format(context
                                                .getCatalog()
                                                .getPluralString(
                                                        "You did not keep your goal in {0}. Better luck next month.",
                                                        "You did not keep a single goal in {0}. Better luck next month.",
                                                        numberOfFollowItems),
                                                monthName));
                    } else if (numberOfPositiveFollowItems > 0) {
                        String budgetDescriptiveFeedback = Catalog.format(
                                context.getCatalog().getString(
                                        "You kept {0} of your goals in {1}."),
                                numberOfPositiveFollowItems, monthName);

                        followFeedback
                                .setFeedbackTitle(budgetDescriptiveFeedback
                                        + " "
                                        + buildBudgetSentiment(
                                                ratioOfPassedBudgets, context));
                    }

                    followFeedback.setFollowItems(Lists.newArrayList(periodFollowItems));

                    summaryData.setFollowFeedback(Lists.newArrayList(followFeedback));
                }
            }

            // Expenses by period

            ImmutableListMultimap<String, Statistic> statisticsByType = Multimaps.index(
                    statistics,
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

            final String uncategorizedCategoryId = Iterables.find(
                    context.getCategories(), c -> Objects
                            .equal(context.getCategoryConfiguration().getExpenseUnknownCode(), c.getCode())).getId();

            Iterable<Statistic> largestCategoryExpenses = STATISTICS_VALUE_ORDERING.leastOf(
                    Iterables.filter(monthInterestingExpensesByCategory,
                            s -> (!Objects.equal(s.getDescription(), uncategorizedCategoryId))), 3);

            List<MonthlySummaryActivityCategoryData> largestCategories = Lists.newArrayList();

            List<KVPair<Statistic, Double>> monthInterestingExpensesByCategoryWithAvg = Lists.newArrayList();
            for (Statistic s : monthInterestingExpensesByCategory) {
                double categoryAvg = getCategoryPeriodAverage(s.getDescription(), sortedPeriods, periodIndex,
                        interestingExpensesByPeriodStatistics);

                KVPair<Statistic, Double> monthInterestingExpenseWithAvg = new KVPair<Statistic, Double>(s, categoryAvg);
                monthInterestingExpensesByCategoryWithAvg.add(monthInterestingExpenseWithAvg);
            }

            Iterable<KVPair<Statistic, Double>> largestDifferenceToAverageCategoryExpenses = STATISTICS_AVG_ORDERING
                    .leastOf(
                            Iterables.filter(monthInterestingExpensesByCategoryWithAvg,
                                    kvpair -> (!Objects.equal(kvpair.getKey().getDescription(),
                                            uncategorizedCategoryId))), 3);

            List<MonthlySummaryActivityCategoryData> unusualSpendings = Lists.newArrayList();

            for (KVPair<Statistic, Double> s : largestDifferenceToAverageCategoryExpenses) {
                final MonthlySummaryActivityCategoryData categorySummaryData = new MonthlySummaryActivityCategoryData();

                categorySummaryData.setCategoryId(s.getKey().getDescription());
                categorySummaryData.setAmount(s.getKey().getValue());
                categorySummaryData.setAverage(s.getValue());

                unusualSpendings.add(categorySummaryData);
            }

            for (Statistic s : largestCategoryExpenses) {
                final MonthlySummaryActivityCategoryData categorySummaryData = new MonthlySummaryActivityCategoryData();

                categorySummaryData.setCategoryId(s.getDescription());
                categorySummaryData.setAmount(s.getValue());
                categorySummaryData.setCount((int) Iterables.find(
                        monthInterestingExpensesByCategoryCount,
                        s1 -> (s1.getDescription()
                                .equals(categorySummaryData
                                        .getCategoryId()))).getValue());

                double previousSpendingsSum = 0;

                double[] previousSpendings;
                try {
                    previousSpendings = descriptiveStatisticsByCategoryId.get(
                            s.getDescription()).getValues();
                    for (double previousSpending : previousSpendings) {
                        previousSpendingsSum += previousSpending;
                    }
                    categorySummaryData.setAverage(previousSpendingsSum
                            / previousSpendings.length);
                } catch (ExecutionException e) {
                    log.warn(
                            "Could not fetch values from descriptiveStatisticsByCategoryId UserID: "
                                    + context.getUser().getId(), e);
                }

                largestCategories.add(categorySummaryData);

            }

            summaryData.setLargestCategories(largestCategories);
            summaryData.setUnusualSpending(unusualSpendings);
            summaryData.setExpensesAvg(periodExpensesAvg);

            // Create the rest of the data structures.

            summaryData.setPeriod(period.getName());
            summaryData.setIncome(periodIncome);
            summaryData.setExpenses(periodExpenses);
            summaryData.setLargestExpenses(periodLargestExpenses);
            summaryData.setLargestIncome(periodLargestIncome);
            summaryData.setLeftToSpend(leftToSpendStatisticsByPeriod.get(period
                    .getName()));

            summaryData
                    .setLeftToSpendAverage(leftToSpendAverageStatisticsByPeriod
                            .get(period.getName()));
            summaryData.setMonth(periodEndDate.getMonth() + 1);

            // Construct key. This needs to be done _after_ `summaryData` has been populated.
            String notificationKey = createKey(summaryData, period);
            String feedActivityIdentifier = StringUtils.hashAsStringSHA1(notificationKey);
            
            context.addActivity(
                    createActivity(
                            context.getUser().getId(),
                            periodEndDate,
                            Activity.Types.MONTHLY_SUMMARY,
                            StringUtils.formatHuman(monthName),
                            generateMessage(context, monthName, periodNet),
                            summaryData,
                            notificationKey,
                            feedActivityIdentifier));
        }
    }
    
    private static String createKey(MonthlySummaryActivityData summaryData, Period period) {
        String notificationKey = String.format("%s.%s", Activity.Types.MONTHLY_SUMMARY, period.getName()); 
        
        // Add start date and end date to the key if the period is not `MONTHLY` ("calendar month").
        if (!Objects.equal(ResolutionTypes.MONTHLY, period.getResolution())) {
            ThreadSafeDateFormat dateFormatter = ThreadSafeDateFormat.FORMATTER_INTEGER_DATE;
            notificationKey += String.format(".%s-%s", dateFormatter.format(period.getStartDate()),
                    dateFormatter.format(period.getEndDate()));
        }
        
        // Add follow item state to the key.
        if (summaryData.getFollowFeedback() != null && !summaryData.getFollowFeedback().isEmpty()) {
            List<String> followItemKeys = Lists.newArrayList();
            
            // Get id:s and last modified date for all follow items.
            for (FollowActivityFeedbackData data : summaryData.getFollowFeedback()) {
                for (FollowItem item : data.getFollowItems()) {
                    
                    // INVESTIGATE NPE
                    // `item` and `item.getLastModified()` should never be `null` (but apparently they sometimes are).
                    
                    if (item == null) {
                        log.warn("A follow item is `null`.");
                    } else {
                        if (item.getLastModified() == null) {
                            log.warn(item.getUserId(), "The follow item doesn't have a 'last modified' timestamp.");
                            followItemKeys.add(String.format("%s", item.getId()));
                        } else {
                            followItemKeys.add(String.format("%s,%d", item.getId(), item.getLastModified().getTime()));    
                        }
                    }
                }
            }
            
            Collections.sort(followItemKeys);
            
            String followItemsKey = StringUtils.hashAsStringSHA1(FOLLOW_ITEM_KEY_JOINER.join(followItemKeys));
            
            notificationKey += String.format(".FOLLOW_ITEMS-%s", followItemsKey);
        }
        
        return notificationKey;
    }

    private double getCategoryPeriodAverage(String description, List<Period> periods, int periodIndex,
            ListMultimap<String, Statistic> interestingExpensesByPeriodStatistics) {
        int firstPeriod = Math.max(0, periodIndex - PERIOD_COUNT_FOR_AVERAGE_CALCULATION + 1);
        double sum = 0;
        for (int i = firstPeriod; i <= periodIndex; i++) {
            Period period = periods.get(i);
            List<Statistic> stats = interestingExpensesByPeriodStatistics.get(period.getName());
            for (Statistic statistic : stats) {
                if (statistic.getDescription().equals(description)) {
                    sum += statistic.getValue();
                    break;
                }
            }
        }
        return sum / (periodIndex - firstPeriod + 1);
    }

    private double getPeriodExpensesAvg(ImmutableMap<String, Statistic> expensesByPeriod, List<Period> periods,
            int periodIndex) {

        double sum = 0;
        for (int i = Math.max(0, periodIndex - PERIOD_COUNT_FOR_AVERAGE_CALCULATION + 1); i <= periodIndex; i++) {
            Period period = periods.get(i);
            Statistic statistic = expensesByPeriod.get(period
                    .getName());
            sum += (statistic != null) ? statistic.getValue() : 0;
        }
        return sum / Math.min(periodIndex + 1, PERIOD_COUNT_FOR_AVERAGE_CALCULATION);
    }
    
    private static String generateMessage(ActivityGeneratorContext context, String monthName, double periodNetAmount) {
        final Catalog catalog = context.getCatalog();
        boolean shouldGenerateSensitiveMessage = context.getActivitiesConfiguration().shouldGenerateSensitiveMessage();

        if (shouldGenerateSensitiveMessage) {
            String formattedAmount = I18NUtils.formatCurrency(Math.abs(periodNetAmount), context.getUserCurrency(),
                    context.getLocale());

            String format;
            if (periodNetAmount > 0) {
                format = catalog.getString("{0} was a great month! You made {1} more than you spent.");
            } else {
                format = catalog.getString("{0} was a not a great month. You spent {1} more than you made.");
            }

            return Catalog.format(format, StringUtils.firstLetterUppercaseFormatting(monthName), formattedAmount);
        } else {
            return catalog.getString("Check out your monthly summary!");
        }
    }

    @Override
    public boolean isNotifiable() {
        return true;
    }
}
