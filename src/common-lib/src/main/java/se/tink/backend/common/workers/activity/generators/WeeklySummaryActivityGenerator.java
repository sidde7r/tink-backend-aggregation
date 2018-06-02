package se.tink.backend.common.workers.activity.generators;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import se.tink.backend.common.statistics.StatisticsGeneratorAggregator;
import se.tink.backend.common.statistics.StatisticsGeneratorFunctions;
import se.tink.backend.common.statistics.predicates.TransactionPredicate;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.FollowUtils;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.common.workers.activity.generators.models.FollowActivityFeedbackData;
import se.tink.backend.common.workers.activity.generators.models.WeeklySummaryActivityCategoryData;
import se.tink.backend.common.workers.activity.generators.models.WeeklySummaryActivityData;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.KVPair;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.UserData;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.follow.FollowCriteria;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.follow.FollowTypes;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.i18n.Catalog;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Multimaps.index;

public class WeeklySummaryActivityGenerator extends ActivityGenerator {
    private static final LogUtils log = new LogUtils(
            WeeklySummaryActivityGenerator.class);

    private static final Ordering<Statistic> STATISTICS_VALUE_ORDERING = new Ordering<Statistic>() {
        @Override
        public int compare(Statistic left, Statistic right) {
            return Doubles.compare(left.getValue(), right.getValue());
        }
    };

    private static ImmutableSet<String> getUninterestingCategoryIds(final Map<String, Category> categoriesByCode,
            Set<String> weeklySummaryActivityExcludedCategories) {
        return ImmutableSet.copyOf(transform(weeklySummaryActivityExcludedCategories,
                categoryCode -> categoriesByCode.get(categoryCode).getId()));
    }

    private static String buildBudgetSentiment(double ratio,
            ActivityGeneratorContext context) {
        if (ratio > 0.80) {
            return context.getCatalog().getString("Great job!");
        } else if (ratio > 0.5) {
            return context.getCatalog().getString("Good job!");
        } else {
            return context.getCatalog().getString("Come on!");
        }
    }

    public WeeklySummaryActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(WeeklySummaryActivityGenerator.class, 80, 90, deepLinkBuilderFactory);
    }

    @Override
    public void generateActivity(final ActivityGeneratorContext context) {
        
        final String uncategorizedCategoryId = context.getCategoriesByCodeForLocale()
                .get(context.getCategoryConfiguration().getExpenseUnknownCode()).getId();

        ImmutableListMultimap<ResolutionTypes, Statistic> statisticsByResolution = index(context.getStatistics(),
                Statistic::getResolution);
        
        ImmutableListMultimap<String, Statistic> statisticsByTypeWeekly = index(
                statisticsByResolution.get(ResolutionTypes.WEEKLY), Statistic::getType);
        
        ImmutableListMultimap<String, Statistic> statisticsByTypeDaily = index(
                statisticsByResolution.get(ResolutionTypes.DAILY), Statistic::getType);
        
        ListMultimap<String, Statistic> dailyExpensesStatistics = index(
                statisticsByTypeDaily.get(Statistic.Types.EXPENSES_BY_CATEGORY), Statistic::getPeriod);
        
        ListMultimap<String, Statistic> interestingExpensesByPeriodStatistics = index(
                statisticsByTypeWeekly.get(Statistic.Types.EXPENSES_BY_CATEGORY), Statistic::getPeriod);
        
        ListMultimap<String, Statistic> interestingExpensesByPeriodByCountStatistics = index(
                statisticsByTypeWeekly.get(Statistic.Types.EXPENSES_COUNT_BY_CATEGORY),
                Statistic::getPeriod);

        int weekAvgs = 26;

        Calendar weekdayComparisonStartDate = DateUtils.getCalendar();
        weekdayComparisonStartDate.add(Calendar.DATE, -7 * weekAvgs); // Half a year of weeks

        final String start = ThreadSafeDateFormat.FORMATTER_DAILY.format(weekdayComparisonStartDate.getTime());

        Iterable<Statistic> last6m = Iterables.filter(dailyExpensesStatistics.values(),
                s -> (s.getPeriod().compareTo(start) > 0));

        ImmutableListMultimap<Integer, Statistic> expenses6mByDayofweek = Multimaps.index(last6m,
                new Function<Statistic, Integer>() {
                    @Override
                    @Nullable
                    public Integer apply(Statistic s) {
                        Date date;
                        try {
                            date = ThreadSafeDateFormat.FORMATTER_DAILY.parse(s.getPeriod());
                        } catch (ParseException e) {
                            log.warn("Could not parse daily format. Period: " + s.getPeriod(), e);
                            date = new Date();
                        }

                        Calendar c = DateUtils.getCalendar(date);
                        return c.get(Calendar.DAY_OF_WEEK);
                    }
                });

        List<KVPair<Integer, Double>> averagePerWeekday = Lists.newLinkedList();

        for (int day = 1; day <= 7; day++) {
            double sum = 0;
            List<Statistic> statisticsPerDay = expenses6mByDayofweek.get(day);
            for (Statistic statistic : statisticsPerDay) {
                sum += statistic.getValue();
            }
            averagePerWeekday.add(new KVPair<Integer, Double>(day, sum / weekAvgs));
        }

        // Construct the weekly boundary calendars.

        Calendar weekStartCalendar = DateUtils.getCalendar(context.getLocale());
        // Start of current week
        weekStartCalendar = DateUtils.getFirstDateOfWeek(weekStartCalendar);
        // Start of previous week
        weekStartCalendar.add(Calendar.DAY_OF_YEAR, -7);
        DateUtils.setInclusiveStartTime(weekStartCalendar);

        Calendar weekEndCalendar = (Calendar) weekStartCalendar.clone();
        weekEndCalendar.add(Calendar.DAY_OF_YEAR, 6);
        DateUtils.setInclusiveEndTime(weekEndCalendar);

        // Construct weekly boundary for follow feedback.

        for (int i = 0; i < 4; i++) {
            final Date weekEndDate = weekEndCalendar.getTime();
            final Date weekStartDate = weekStartCalendar.getTime();

            final int weekOfYear = weekEndCalendar.get(Calendar.WEEK_OF_YEAR);
            final String weekPeriod;
            final String yearOfWeekPeriod;

            // Highest number weeks always has the year of the date at the start of the week. The opposite for first
            // weeks.
            if (weekOfYear <= 52) {
                yearOfWeekPeriod = ThreadSafeDateFormat.FORMATTER_YEARLY.format(weekEndCalendar.getTime());
            } else {
                yearOfWeekPeriod = ThreadSafeDateFormat.FORMATTER_YEARLY.format(weekStartCalendar.getTime());
            }

            weekPeriod = String.format("%s:%s", yearOfWeekPeriod,
                    Strings.padStart(Integer.toString(weekOfYear), 2, '0'));

            List<Statistic> weekInterestingExpensesByCategory = interestingExpensesByPeriodStatistics.get(weekPeriod);
            List<Statistic> weekInterestingExpensesByCategoryCount = interestingExpensesByPeriodByCountStatistics
                    .get(weekPeriod);

            if (weekInterestingExpensesByCategory.isEmpty()) {
                weekEndCalendar.add(Calendar.WEEK_OF_YEAR, -1);
                weekStartCalendar.add(Calendar.WEEK_OF_YEAR, -1);
                continue;
            }

            WeeklySummaryActivityData summaryData = new WeeklySummaryActivityData();
            summaryData.setWeekEndDate(weekEndDate);
            summaryData.setWeekStartDate(weekStartDate);
            summaryData.setHistoricalExpensesAverage(averagePerWeekday);

            if (!context.getFollowItems().isEmpty()) {
                FollowActivityFeedbackData followFeedback = generateFollowFeedbackData(context, weekEndDate);

                if (followFeedback != null) {
                    summaryData.setFollowFeedback(Lists.newArrayList(followFeedback));
                }
            }

            List<Transaction> weekExpenses = Lists.newArrayList(filter(context.getTransactions(),
                    t -> (t.getDate().after(weekStartDate) && t.getDate().before(weekEndDate) && Objects
                            .equal(t.getCategoryType(), CategoryTypes.EXPENSES))));

            double weekExpensesAmount = 0;
            int weekExpensesCount = weekExpenses.size();

            Transaction weekStartDummyTransaction = new Transaction();
            weekStartDummyTransaction.setAccountId(context.getAccounts().get(0).getId());
            weekStartDummyTransaction.setCategoryType(CategoryTypes.EXPENSES);
            weekStartDummyTransaction.setDate(weekStartDate);

            Transaction weekEndDummyTransaction = new Transaction();
            weekEndDummyTransaction.setAccountId(context.getAccounts().get(0).getId());
            weekEndDummyTransaction.setCategoryType(CategoryTypes.EXPENSES);
            weekEndDummyTransaction.setDate(weekEndDate);

            weekExpenses.add(weekStartDummyTransaction);
            weekExpenses.add(weekEndDummyTransaction);

            final Set<String> excludedCategoryIds = getUninterestingCategoryIds(context.getCategoriesByCodeForLocale(),
                                                                                context.getCategoryConfiguration()
                                                                                       .getWeeklySummaryActivityExcludedCodes());

            // Calculate largest transaction/expense (the transaction with smallest amount/most negative).
            final Transaction biggestExpense = weekExpenses.stream()
                                                           // only interested in actual expenses
                                                           .filter(t -> t.getAmount() < 0)
                                                           .filter(t -> !excludedCategoryIds.contains(t.getCategoryId()))
                                                           .min(Comparator.comparing(Transaction::getAmount))
                                                           .orElse(null);


            // Calculate the weeks daily expenses.

            UserData weekStatisticsGeneratorContext = new UserData();

            weekStatisticsGeneratorContext.setUser(context.getUser());
            weekStatisticsGeneratorContext.setCredentials(context.getCredentials());
            weekStatisticsGeneratorContext.setAccounts(context.getAccounts());
            weekStatisticsGeneratorContext.setTransactions(weekExpenses);

            List<Transaction> transactionsFiltered = Lists
                    .newArrayList(Iterables.filter(weekExpenses, new TransactionPredicate(
                            context.getAccounts(), context.getCategories(), context.getCategoryConfiguration())));

            List<Statistic> statistics = StatisticsGeneratorAggregator.aggregateUserTransactionStatistics(
                    weekStatisticsGeneratorContext,
                    Statistic.Types.INCOME_AND_EXPENSES, ResolutionTypes.DAILY,
                    transactionsFiltered, weekExpenses,
                    StatisticsGeneratorFunctions.STATISTICS_SUM_FUNCTION, null,
                    StatisticsGeneratorFunctions.STATISTICS_GROUP_FUNCTION,
                    StatisticsGeneratorFunctions.TRANSACTION_CATEGORY_TYPE_FUNCTION);

            for (Statistic s : statistics) {
                weekExpensesAmount += s.getValue();
            }

            // Calculate weekly expense average

            double weekExpensesAverageAmount = 0d;
            for (KVPair<Integer, Double> a : averagePerWeekday) {
                weekExpensesAverageAmount += a.getValue();
            }

            // Construct the summary data structure.

            summaryData.setExpensesAmount(weekExpensesAmount);
            summaryData.setExpensesAverageAmount(weekExpensesAverageAmount);
            summaryData.setExpensesCount(weekExpensesCount);
            summaryData.setBiggestExpense(biggestExpense);

            summaryData.setHistoricalExpenses(zeroFillActivityDataPoints(Lists.newArrayList(transform(
                    statistics,
                    s -> new KVPair<String, Double>(s.getPeriod(), s.getValue()))), ResolutionTypes.DAILY));

            Iterable<Statistic> largestCategoryExpenses = STATISTICS_VALUE_ORDERING.leastOf(
                    filter(weekInterestingExpensesByCategory,
                            s -> (!Objects.equal(s.getDescription(), uncategorizedCategoryId))), 3);

            List<WeeklySummaryActivityCategoryData> largestCategories = Lists.newArrayList();

            for (Statistic s : largestCategoryExpenses) {
                final WeeklySummaryActivityCategoryData categorySummaryData = new WeeklySummaryActivityCategoryData();

                categorySummaryData.setCategoryId(s.getDescription());
                categorySummaryData.setAmount(s.getValue());
                categorySummaryData.setCount((int) find(weekInterestingExpensesByCategoryCount,
                        s1 -> Objects.equal(s1.getDescription(), categorySummaryData.getCategoryId())).getValue());

                largestCategories.add(categorySummaryData);
            }

            summaryData.setLargestCategories(largestCategories);
            summaryData.setWeek(weekOfYear);

            String title;

            if (Objects.equal(context.getMarket().getCodeAsString(), "SE")) {
                title = Catalog.format(context.getCatalog().getString("Week {0}"), weekOfYear);
            } else {
                title = context.getCatalog().getString("Weekly Summary");
            }

            String key = String.format("%s.week%d", Activity.Types.WEEKLY_SUMMARY, weekOfYear);

            String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);

            context.addActivity(
                    createActivity(
                            context.getUser().getId(),
                            weekEndDate,
                            Activity.Types.WEEKLY_SUMMARY,
                            title,
                            generateMessage(context, weekExpensesAmount),
                            summaryData,
                            key,
                            feedActivityIdentifier));

            weekEndCalendar.add(Calendar.WEEK_OF_YEAR, -1);
            weekStartCalendar.add(Calendar.WEEK_OF_YEAR, -1);
        }
    }

    private FollowActivityFeedbackData generateFollowFeedbackData(ActivityGeneratorContext context,
            final Date weekEndDate) {
        
        String period = UserProfile.ProfileDateUtils.getMonthPeriod(weekEndDate, context.getUser().getProfile());

        List<FollowItem> followItems = FollowUtils.cloneFollowItems(Lists.newArrayList(filter(context.getFollowItems(),
                f -> {
                    FollowCriteria criteria = f.getFollowCriteria();
                    return ((Objects.equal(f.getType(), FollowTypes.EXPENSES) || Objects.equal(f.getType(),
                            FollowTypes.SEARCH)) && criteria.getTargetAmount() != null);
                })));

        if (followItems.isEmpty()) {
            return null;
        }

        FollowUtils.populateFollowItems(
                followItems,
                period,
                period,
                weekEndDate,
                false, // Include historical amounts
                false, // Include transactions
                false, // Suggest
                context.getUser(),
                context.getTransactions(),
                context.getTransactionsBySearchFollowItemId(),
                context.getAccounts(),
                context.getStatistics(),
                context.getCategories(),
                context.getCategoryConfiguration());

        int numberOfFollowItems = size(followItems);

        // Check status on follow items and create the follow summary object.

        FollowActivityFeedbackData followFeedback = new FollowActivityFeedbackData();

        if (numberOfFollowItems > 0) {
            Iterable<FollowItem> positiveFollowItems = filter(followItems, b -> (b.isProgressPositive()));

            int numberOfPositiveFollowItems = size(positiveFollowItems);

            double ratioOfPassedBudgets = (double) numberOfPositiveFollowItems / (double) numberOfFollowItems;

            if (numberOfPositiveFollowItems == numberOfFollowItems) {
                followFeedback.setFeedbackTitle(context.getCatalog().getPluralString(
                        "You are good with your goals. Great job!",
                        "You are good on all your goals. Great job!",
                        numberOfFollowItems));
                
            } else if (numberOfPositiveFollowItems == 0) {
                followFeedback.setFeedbackTitle(context.getCatalog().getPluralString(
                        "You have already blown your goal. Better luck next month.",
                        "You have already blown all of your goals. Better luck next month.",
                        numberOfFollowItems));
                
            } else if (numberOfPositiveFollowItems > 0) {
                String budgetDescriptiveFeedback = Catalog.format(
                        context.getCatalog().getString("You are good on {0} of your {1} goals."),
                        numberOfPositiveFollowItems, numberOfFollowItems);

                followFeedback.setFeedbackTitle(Catalog.format("{0} {1}", budgetDescriptiveFeedback,
                        buildBudgetSentiment(ratioOfPassedBudgets, context)));
            }

            followFeedback.setFollowItems(followItems);
        }

        return followFeedback;
    }
    
    private static String generateMessage(ActivityGeneratorContext context, double weekExpensesAmount) {
        final Catalog catalog = context.getCatalog();
        boolean shouldGenerateSensitiveMessage = context.getActivitiesConfiguration().shouldGenerateSensitiveMessage();

        if (shouldGenerateSensitiveMessage) {
            String formattedAmount = I18NUtils.formatCurrency(Math.abs(weekExpensesAmount), context.getUserCurrency(),
                    context.getLocale());

            return Catalog.format(catalog.getString("You have spent {0} during last week."), formattedAmount);
        } else {
            return catalog.getString("Check it out!");
        }
    }

    @Override
    public boolean isNotifiable() {
        return true;
    }
}
