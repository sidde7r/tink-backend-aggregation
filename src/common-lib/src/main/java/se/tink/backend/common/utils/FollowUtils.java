package se.tink.backend.common.utils;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.common.search.TransactionsSearcher;
import se.tink.backend.common.statistics.StatisticsGeneratorAggregator;
import se.tink.backend.common.statistics.StatisticsGeneratorFunctions;
import se.tink.backend.common.statistics.functions.MonthlyAdjustedPeriodizationFunction;
import se.tink.backend.common.statistics.predicates.TransactionPredicate;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.SearchQuery;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StringDoublePair;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.backend.core.follow.ExpensesFollowCriteria;
import se.tink.backend.core.follow.FollowData;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.follow.FollowTypes;
import se.tink.backend.core.follow.SavingsFollowCriteria;
import se.tink.backend.core.follow.SearchFollowCriteria;
import se.tink.backend.rpc.SearchResponse;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class FollowUtils {
    private static final int MAX_SEARCH_RESULTS = 2000;
    private static final int MINIMUM_SAVINGS_INCREASE = 3000;
    private static final int DEFAULT_NUMBER_OF_ADDITIONAL_SAVINGS_PERIODS = 2;
    private static final int HISTORICAL_AMOUNT_PERIODS = 12;

    public static List<FollowItem> cloneFollowItems(List<FollowItem> followItems) {
        return Lists.newArrayList(Iterables.transform(followItems, f -> (f.clone())));
    }

    /**
     *
     * @param period
     * @param currentPeriod
     * @param currentPeriodEndDate
     * @param includeTransactions
     * @param user
     * @param transactions
     * @param accounts
     * @param categories
     * @return
     */
    static FollowData constructTransactionalFollowItemData(final String period, final String currentPeriod,
            final Date currentPeriodEndDate, boolean includeTransactions, final User user,
            List<Transaction> transactions, List<Account> accounts, List<Category> categories,
            CategoryConfiguration categoryConfiguration) {

        final Date statisticCalculationFirstDate = DateUtils.getFirstDateFromPeriod(UserProfile.ProfileDateUtils.getMonthPeriod(
               DateUtils.addMonths(DateUtils.parseDate(currentPeriod), -HISTORICAL_AMOUNT_PERIODS + 1),
                user.getProfile()));

        final Date periodStartDate = UserProfile.ProfileDateUtils.getFirstDateFromPeriod(period, user.getProfile());
        final Date periodEndDate = UserProfile.ProfileDateUtils.getLastDateFromPeriod(period, user.getProfile());

        Predicate<Transaction> transactionPredicate = new TransactionPredicate(accounts, categories,
                categoryConfiguration);

        // Filter transactions based on date.
        Iterable<Transaction> filteredTransactions = filterBetweenDateRange(transactions, statisticCalculationFirstDate,
                currentPeriodEndDate);

        final Iterable<Transaction> statisticsTransactions = Iterables
                .filter(filteredTransactions, transactionPredicate);

        // Filter transaction for PeriodAmounts
        Iterable<Transaction> periodTransactions;
        if (statisticCalculationFirstDate.after(periodStartDate)) {
            periodTransactions = filterBetweenDateRange(transactions, periodStartDate, periodEndDate);
            periodTransactions = Iterables.filter(periodTransactions, transactionPredicate);
        } else {

            periodTransactions = filterBetweenDateRange(statisticsTransactions, periodStartDate, periodEndDate);
        }

        // Construct the follow item data.

        FollowData followItemData = new FollowData(period);
        followItemData.setHistoricalAmounts(
                getHistoricalAmounts(currentPeriod, user, Lists.newArrayList(statisticsTransactions), accounts));

        // Used for PeriodAmounts as we do not want to pad end of month
        // currentPeriodEndDate is 'today'
        Date periodEndDateOrToday = periodEndDate.after(currentPeriodEndDate) ? currentPeriodEndDate : periodEndDate;

        followItemData.setPeriodAmounts(
                getPeriodAmounts(periodStartDate, periodEndDateOrToday, user, Lists.newArrayList(periodTransactions),
                        accounts));

        // Add transactions.

        if (includeTransactions) {
            followItemData.setPeriodTransactions(Lists.newArrayList(periodTransactions));
        }

        followItemData.setPeriodProgress(getPeriodProgress(user, period, currentPeriod, currentPeriodEndDate));

        return followItemData;
    }

    /**
     * Helper method to fan out a list of categories, ie. if one of them is a non-leaf category, replace it with all its
     * sub-categories.
     *
     * @param categories
     * @return
     */
    private static Iterable<Category> fanOutCategories(Iterable<Category> categories, Iterable<Category> allCategories) {
        ImmutableListMultimap<String, Category> categoriesByParentId = Multimaps.index(
                Iterables.filter(allCategories, c -> (!Strings.isNullOrEmpty(c.getParent()))), c -> (c.getParent()));

        List<Category> fannedOutCategories = Lists.newArrayList();

        for (Category category : categories) {
            if (Strings.isNullOrEmpty(category.getSecondaryName())) {
                fannedOutCategories.addAll(categoriesByParentId.get(category.getId()));
            } else {
                fannedOutCategories.add(category);
            }
        }

        return fannedOutCategories;
    }

    // inclusive
    private static Iterable<Transaction> filterBetweenDateRange(Iterable<Transaction> transactions,
            final Date firstDate, final Date lastDate) {
        return Iterables.filter(transactions, transaction -> {
            if (transaction == null) {
                return false;
            }
            return !transaction.getDate().before(firstDate) && !transaction.getDate().after(lastDate);
        });
    }

    private static List<StringDoublePair> getHistoricalAmounts(final String currentPeriod, final User user,
            List<Transaction> transactions, List<Account> accounts) {
        Iterable<Statistic> monthlyStatistics = getStatistics(user, transactions, accounts,
                user.getProfile().getPeriodMode());

        return DataUtils.zeroFill(
                // Make sure that the current period is included in the series.
                DataUtils.pad(
                        DataUtils.transformStatisticsToKVPairs(monthlyStatistics),
                        Sets.newHashSet(currentPeriod)),
                ResolutionTypes.MONTHLY);
    }

    private static List<StringDoublePair> getPeriodAmounts(Date periodStartDate,
            Date periodEndDate, final User user, List<Transaction> periodTransactions, List<Account> accounts) {
        List<Statistic> dailyStatistics = getStatistics(user, periodTransactions, accounts, ResolutionTypes.DAILY);

        return  // Cumulative sum.
                DataUtils.cumSum(
                        // Fill with zero values between actual data points.
                        DataUtils.zeroFill(
                                // Make sure that start and end date are included in the series.
                                DataUtils.pad(
                                        DataUtils.transformStatisticsToKVPairs(dailyStatistics),
                                        Sets.newHashSet(
                                                ThreadSafeDateFormat.FORMATTER_DAILY.format(periodStartDate),
                                                ThreadSafeDateFormat.FORMATTER_DAILY.format(periodEndDate))),
                                ResolutionTypes.DAILY));
    }

    private static double getPeriodProgress(User user, String period, String currentPeriod, Date currentPeriodEndDate) {
        if (Objects.equal(period, currentPeriod)) {
            return DateUtils.getMonthPeriodProgress(currentPeriod, currentPeriodEndDate, user
                    .getProfile().getPeriodMode(), user.getProfile().getPeriodAdjustedDay());
        } else {
            return 1d;
        }
    }

    private static List<Statistic> getStatistics(final User user, List<Transaction> transactions,
            List<Account> accounts, ResolutionTypes resolutionType) {
        UserData userData = new UserData();

        userData.setUser(user);
        userData.setAccounts(accounts);
        userData.setTransactions(transactions);

        MonthlyAdjustedPeriodizationFunction monthlyAdjustedPeriodizationFunction = new MonthlyAdjustedPeriodizationFunction(
                user.getProfile().getPeriodAdjustedDay());

        return StatisticsGeneratorAggregator.aggregateUserTransactionStatistics(userData,
                Statistic.Types.INCOME_AND_EXPENSES_AND_TRANSFERS, resolutionType, transactions, transactions,
                StatisticsGeneratorFunctions.STATISTICS_SUM_FUNCTION, monthlyAdjustedPeriodizationFunction,
                StatisticsGeneratorFunctions.STATISTICS_PERIOD_GROUP_FUNCTION,
                StatisticsGeneratorFunctions.TRANSACTION_CATEGORY_TYPE_FUNCTION);
    }

    /**
     * Populates an expenses follow item with all the required data.
     * @param followItem
     * @param period
     * @param includeTransactions
     * @param suggest
     * @param user
     * @param sessionId
     * @param statistics
     * @param userData
     */
    public static void populateExpensesFollowItem(FollowItem followItem, String period, String currentPeriod,
            Date currentPeriodEndDate, boolean includeTransactions, boolean suggest, final User user,
            List<Transaction> transactions, List<Account> accounts, List<Category> categories,
            CategoryConfiguration categoryConfiguration) {

        final Set<String> followItemCategoryIds = Sets.newHashSet(SerializationUtils.deserializeFromString(
                followItem.getCriteria(), ExpensesFollowCriteria.class).getCategoryIds());

        // Fan out the category IDs and fetch the category IDs of any child-categories.

        final Set<String> fannedOutFollowItemCategoryIds = Sets.newHashSet(Iterables.transform(
                fanOutCategories(Iterables.filter(categories, c -> (followItemCategoryIds.contains(c.getId()))),
                        categories), Category::getId));



        List<Transaction> followItemTransactions = Lists.newArrayList(Iterables.filter(transactions,
                t -> (fannedOutFollowItemCategoryIds.contains(t.getCategoryId()))));

        followItem.setData(constructTransactionalFollowItemData(period, currentPeriod, currentPeriodEndDate,
                includeTransactions, user, followItemTransactions, accounts, categories, categoryConfiguration));

        if (suggest) {
            ExpensesFollowCriteria followItemCriteria = SerializationUtils.deserializeFromString(
                    followItem.getCriteria(), ExpensesFollowCriteria.class);

            double avg = DataUtils.calculateAverageAmount(followItem.getData().getHistoricalAmounts());
            followItemCriteria.setTargetAmount(avg);

            followItem.setCriteria(SerializationUtils.serializeToString(followItemCriteria));
        }
    }

    /**
     *
     * @param followItem
     * @param period
     * @param currentPeriod
     * @param currentPeriodEndDate
     * @param includeHistoricalAmounts
     * @param includeTransactions
     * @param suggest
     * @param user
     * @param transactions
     * @param transactionsBySearchFollowItemId
     * @param accounts
     * @param statistics
     * @param categories
     */
    public static void populateFollowItem(FollowItem followItem, String period, String currentPeriod,
            Date currentPeriodEndDate, boolean includeHistoricalAmounts, boolean includeTransactions, boolean suggest,
            User user, List<Transaction> transactions, Map<String, List<Transaction>> transactionsBySearchFollowItemId,
            List<Account> accounts, List<Statistic> statistics, List<Category> categories, CategoryConfiguration categoryConfiguration) {

        FollowUtils.populateFollowItems(Lists.newArrayList(followItem), period, currentPeriod, currentPeriodEndDate,
                includeHistoricalAmounts, includeTransactions, suggest, user, transactions,
                transactionsBySearchFollowItemId, accounts, statistics, categories, categoryConfiguration);
    }

    /**
     *
     *
     * @param followItems
     * @param period
     * @param currentPeriod
     * @param currentPeriodEndDate
     * @param includeHistoricalAmounts Only used by `SAVINGS`.
     * @param includeTransactions
     * @param suggest
     * @param user
     * @param transactions
     * @param transactionsBySearchFollowItemId
     * @param accounts
     * @param statistics Only used by `SAVINGS`.
     * @param categories
     */
    public static void populateFollowItems(List<FollowItem> followItems, String period, String currentPeriod,
            Date currentPeriodEndDate, boolean includeHistoricalAmounts, boolean includeTransactions, boolean suggest,
            User user, List<Transaction> transactions, Map<String, List<Transaction>> transactionsBySearchFollowItemId,
            List<Account> accounts, List<Statistic> statistics, List<Category> categories,
            CategoryConfiguration categoryConfiguration) {
        for (FollowItem followItem : followItems) {
            switch (followItem.getType()) {
            case EXPENSES:
                FollowUtils.populateExpensesFollowItem(followItem, period, currentPeriod, currentPeriodEndDate,
                        includeTransactions, suggest, user, transactions, accounts, categories, categoryConfiguration);
                break;
            case SAVINGS:
                FollowUtils.populateSavingsFollowItem(followItem, period, currentPeriod, currentPeriodEndDate,
                        includeHistoricalAmounts, includeTransactions, suggest, user, transactions, statistics,
                        categories);
                break;
            case SEARCH:
                FollowUtils.populateSearchFollowItem(followItem, period, currentPeriod, currentPeriodEndDate,
                        includeTransactions, suggest, user, transactionsBySearchFollowItemId.get(followItem.getId()),
                        accounts, categories, categoryConfiguration);
                break;
            }
        }
    }

    /**
     * Populates a savings follow item with all the required data.
     */
    public static void populateSavingsFollowItem(FollowItem followItem, String period, String currentPeriod,
            Date currentPeriodEndDate, boolean includeHistoricalAmounts, boolean includeTransactions, boolean suggest,
            final User user, List<Transaction> transactions, List<Statistic> statistics, List<Category> categories) {
        FollowData followItemData = new FollowData(period);

        SavingsFollowCriteria followItemCriteria = SerializationUtils.deserializeFromString(followItem.getCriteria(),
                SavingsFollowCriteria.class);

        final Set<String> followItemAccountIds = Sets.newHashSet(followItemCriteria.getAccountIds());

        if (includeHistoricalAmounts) {
            final Date futureCurrentPeriodEndDate = UserProfile.ProfileDateUtils.getLastDateFromPeriod(currentPeriod, user.getProfile());

            Iterable<Statistic> followItemHistoricalStatistics = Iterables.filter(statistics,
                    s -> {
                        if (!Objects.equal(s.getType(), Statistic.Types.BALANCES_BY_ACCOUNT)
                                || !followItemAccountIds.contains(s.getDescription())
                                || s.getResolution() != user.getProfile().getPeriodMode()) {
                            return false;
                        }

                        Date periodEndDate = UserProfile.ProfileDateUtils.getLastDateFromPeriod(s.getPeriod(), user.getProfile());

                        return (!periodEndDate.after(futureCurrentPeriodEndDate));
                    });

            followItemData.setHistoricalAmounts(DataUtils.limit(
                    DataUtils.zeroFill(DataUtils.pad(DataUtils.aggregateStatisticsToKVPairs(followItemHistoricalStatistics), Sets.newHashSet(period)),
                            ResolutionTypes.MONTHLY), 12));
        }

        if (includeTransactions) {
            followItemData.setPeriodTransactions(Lists.newArrayList(Iterables.filter(transactions,
                    t -> (followItemAccountIds.contains(t.getAccountId())))));
        }

        followItem.setData(followItemData);

        // Suggest the targetAmount and targetPeriod.

        if (suggest) {
            List<StringDoublePair> historicalAmounts = followItemData.getHistoricalAmounts();

            SimpleRegression regressionModel = new SimpleRegression();
            DescriptiveStatistics movingWindowStatistics = new DescriptiveStatistics(2);

            for (int i = 0; i < historicalAmounts.size(); i++) {
                regressionModel.addData(i, historicalAmounts.get(i).getValue());
                movingWindowStatistics.addValue(historicalAmounts.get(i).getValue());
            }

            double suggestedAmountFloor = Math.max(0, movingWindowStatistics.getMax()) + MINIMUM_SAVINGS_INCREASE;

            // TODO: Implement currencyFactor when we merge US branch.

            double predictedAmount = regressionModel.predict(historicalAmounts.size() - 1
                    + DEFAULT_NUMBER_OF_ADDITIONAL_SAVINGS_PERIODS);

            if (Double.isNaN(predictedAmount)) {
                predictedAmount = 0;
            }

            double targetAmount = (Math.ceil(Math.max(suggestedAmountFloor, predictedAmount) / 100) * 100);

            Calendar calendar = DateUtils.getCalendar();
            calendar.setTime(currentPeriodEndDate);
            calendar.add(Calendar.MONTH, DEFAULT_NUMBER_OF_ADDITIONAL_SAVINGS_PERIODS);

            String targetPeriod = UserProfile.ProfileDateUtils.getMonthPeriod(calendar.getTime(), user.getProfile());

            followItemCriteria.setTargetAmount(targetAmount);
            followItemCriteria.setTargetPeriod(targetPeriod);

            followItem.setCriteria(SerializationUtils.serializeToString(followItemCriteria));
        }
    }

    /**
     * Populate a search follow item by executing a query.
     */
    public static void populateSearchFollowItem(FollowItem followItem, final String period, String currentPeriod,
            Date currentPeriodEndDate, boolean includeTransactions, boolean suggest, final User user,
            List<Transaction> transactions, List<Account> accounts, List<Category> categories,
            CategoryConfiguration categoryConfiguration) {
        // Create the follow data structure.

        followItem.setData(constructTransactionalFollowItemData(period, currentPeriod, currentPeriodEndDate,
                includeTransactions, user, transactions, accounts, categories, categoryConfiguration));

        if (suggest) {
            SearchFollowCriteria followItemCriteria = SerializationUtils.deserializeFromString(
                    followItem.getCriteria(), SearchFollowCriteria.class);

            double avg = DataUtils.calculateAverageAmount(followItem.getData().getHistoricalAmounts());
            followItemCriteria.setTargetAmount(avg);

            followItem.setCriteria(SerializationUtils.serializeToString(followItemCriteria));
        }
    }

    public static Map<String, List<Transaction>> querySearchFollowItemsTransactions(List<FollowItem> followItems,
            User user, TransactionsSearcher transactionsSearcher) {
        Map<String, List<Transaction>> transactionsBySearchFollowItemId = Maps.newHashMap();

        for (FollowItem followItem : followItems) {
            if (followItem.getType() != FollowTypes.SEARCH) {
                continue;
            }

            String queryString = SerializationUtils.deserializeFromString(followItem.getCriteria(),
                    SearchFollowCriteria.class).getQueryString();

            SearchQuery searchQuery = new SearchQuery();
            searchQuery.setQueryString(queryString);
            searchQuery.setLimit(MAX_SEARCH_RESULTS);

            SearchResponse searchResponse = transactionsSearcher.query(user, searchQuery);

            transactionsBySearchFollowItemId.put(
                    followItem.getId(),
                    Lists.newArrayList(Iterables.filter(
                            Iterables.transform(searchResponse.getResults(), sr -> (sr.getTransaction())),
                            t -> (t.getAmount() < 0))));
        }

        return transactionsBySearchFollowItemId;
    }
}
