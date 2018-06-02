package se.tink.backend.common.statistics;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.common.statistics.functions.AccountBalanceToAccountTypeFunction;
import se.tink.backend.common.statistics.functions.MonthlyAdjustedPeriodizationFunction;
import se.tink.backend.common.statistics.functions.StatisticsLeftToSpendFunction;
import se.tink.backend.common.statistics.functions.StatisticsSumming;
import se.tink.backend.common.statistics.functions.lefttospendaverage.StatisticsLeftToSpendAverageFunction;
import se.tink.backend.common.statistics.predicates.TransactionExpensesPredicate;
import se.tink.backend.common.statistics.predicates.TransactionIncomeAndExpensesPredicate;
import se.tink.backend.common.statistics.predicates.TransactionIncomePredicate;
import se.tink.backend.common.statistics.predicates.TransactionPredicate;
import se.tink.backend.common.statistics.predicates.TransactionSavingsPredicate;
import se.tink.backend.common.statistics.predicates.NoUpcomingTransactionPredicate;
import se.tink.backend.core.Category;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.UserData;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;
import se.tink.libraries.uuid.UUIDUtils;

public class StatisticsGenerator {

    private final ExecutorService executor;
    private final MetricRegistry registry;
    private final Cluster cluster;
    private final List<Category> categories;
    private final ImmutableMap<String, Category> categoriesById;
    private final CategoryConfiguration categoryConfiguration;

    public StatisticsGenerator(List<Category> categories, CategoryConfiguration categoryConfiguration,
                               MetricRegistry registry, Cluster cluster) {
        this(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()),
                categories, categoryConfiguration,registry, cluster);
    }

    @VisibleForTesting
    public StatisticsGenerator(ExecutorService executor, List<Category> categories, CategoryConfiguration categoryConfiguration,
                               MetricRegistry registry, Cluster cluster) {
        this.executor = executor;
        this.categories = categories;
        this.registry = registry;
        this.cluster = cluster;
        this.categoryConfiguration = categoryConfiguration;

        ImmutableMap.Builder<String, String> parentCategoryBuilder = ImmutableMap.builder();

        for (Category category : categories) {
            if (category.getParent() != null) {
                parentCategoryBuilder.put(category.getId(), category.getParent());
            }
        }

        categoriesById = Maps.uniqueIndex(categories, Category::getId);
    }

    public void stop() {
        executor.shutdown();
    }

    public void populateUserDataWithStatistics(UserData userData) {
        MonthlyAdjustedPeriodizationFunction monthlyAdjustedPeriodizationFunction = new MonthlyAdjustedPeriodizationFunction(
                userData.getUser().getProfile().getPeriodAdjustedDay());

        List<Future<List<Statistic>>> statisticFutures =
                accountHistoryStatistics(userData, monthlyAdjustedPeriodizationFunction);
        statisticFutures.addAll(transactionStatistics(userData, monthlyAdjustedPeriodizationFunction));
        userData.addStatistics(statisticFutures.stream()
                .map(StatisticsGenerator::unwrapStatisticsFuture)
                .flatMap(List::stream)
                .collect(Collectors.toList()));
        userData.addStatistics(generateStatisticsBasedStatistics(userData, monthlyAdjustedPeriodizationFunction, 6));
        populateUserDataWithLoanStatistics(userData);
    }

    @VisibleForTesting
    static List<Statistic> unwrapStatisticsFuture(Future<List<Statistic>> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new StatisticException("Statistic calculation interrupted");
        } catch (ExecutionException e) {
            throw new StatisticException(e);
        }
    }

    @VisibleForTesting
    public List<Future<List<Statistic>>> accountHistoryStatistics(final UserData userData,
                                                                  final MonthlyAdjustedPeriodizationFunction monthlyAdjustedPeriodizationFunction) {
        List<Future<List<Statistic>>> statisticFutures = new ArrayList<>(2);
        ResolutionTypes monthlyType = userData.getUser().getProfile().getPeriodMode();

        statisticFutures.add(executor.submit(() ->
                applyResolutionTypesToFunction(Statistic.Types.BALANCES_BY_ACCOUNT,
                        resolutionTypes -> StatisticsGeneratorAggregator.aggregateAccountBalances(
                                userData,
                                Statistic.Types.BALANCES_BY_ACCOUNT,
                                resolutionTypes,
                                monthlyAdjustedPeriodizationFunction,
                                accountBalance -> UUIDUtils
                                        .toTinkUUID(accountBalance.getAccountId())),
                        monthlyType, ResolutionTypes.DAILY)));

        AccountBalanceToAccountTypeFunction balanceToAccountType = new AccountBalanceToAccountTypeFunction(
                userData.getAccounts());
        statisticFutures.add(executor.submit(() ->
                applyResolutionTypesToFunction(Statistic.Types.BALANCES_BY_ACCOUNT_TYPE_GROUP,
                        resolutionTypes -> StatisticsGeneratorAggregator.aggregateAccountBalances(
                                userData,
                                Statistic.Types.BALANCES_BY_ACCOUNT_TYPE_GROUP,
                                resolutionTypes,
                                monthlyAdjustedPeriodizationFunction,
                                balanceToAccountType,
                                StatisticsGeneratorFunctions.STATISTICS_GROUP_FUNCTION,
                                StatisticsSumming::reduce),
                        ResolutionTypes.WEEKLY, ResolutionTypes.DAILY)));
        return statisticFutures;
    }

    @SuppressWarnings("unused")
    List<Future<List<Statistic>>> transactionStatistics(final UserData userData,
                                                        final MonthlyAdjustedPeriodizationFunction monthlyAdjustedPeriodizationFunction) {
        Timer.Context filterTiming = time("filter_transactions");
        final List<Transaction> transactionIncome = Lists.newArrayList(
                Iterables.filter(Iterables.filter(userData.getTransactions(), new TransactionIncomePredicate()),
                        new NoUpcomingTransactionPredicate(userData.getCredentials())));

        final List<Transaction> includedTransactionIncome = Lists
                .newArrayList(Iterables.filter(transactionIncome,
                        new TransactionPredicate(userData.getAccounts(), categories, categoryConfiguration)));

        final List<Transaction> transactionExpenses = Lists.newArrayList(
                Iterables.filter(Iterables.filter(userData.getTransactions(), new TransactionExpensesPredicate()),
                        new NoUpcomingTransactionPredicate(userData.getCredentials())));

        final List<Transaction> includedTransactionExpenses= Lists
                .newArrayList(Iterables.filter(transactionExpenses,
                        new TransactionPredicate(userData.getAccounts(), categories, categoryConfiguration)));


        final List<Transaction> transactionExpensesWithMerchants = transactionExpenses.stream()
                .filter(t -> categoryConfiguration.getMerchantizeCodes()
                        .contains(categoriesById.get(t.getCategoryId()).getCode()))
                .collect(Collectors.toList());

        final List<Transaction> includedTransactionExpensesWithMerchants = Lists
                .newArrayList(Iterables.filter(transactionExpensesWithMerchants,
                        new TransactionPredicate(userData.getAccounts(), categories, categoryConfiguration)));


        final List<Transaction> transactionIncomeAndExpenses = Lists.newArrayList(
                Iterables.filter(Iterables.filter(
                        userData.getTransactions(), new TransactionIncomeAndExpensesPredicate()),
                        new NoUpcomingTransactionPredicate(userData.getCredentials())));

        final List<Transaction> includedTransactionIncomeAndExpenses = Lists
                .newArrayList(Iterables.filter(transactionIncomeAndExpenses,
                        new TransactionPredicate(userData.getAccounts(), categories, categoryConfiguration)));


        final List<Transaction> transactionSavings = Lists.newArrayList(Iterables.filter(userData.getTransactions(),
                new TransactionSavingsPredicate(categories, categoryConfiguration)));

        final List<Transaction> includedTransactionSavings = Lists
                .newArrayList(Iterables.filter(transactionSavings,
                        new TransactionPredicate(userData.getAccounts(), categories, categoryConfiguration)));

        final ResolutionTypes monthlyType = userData.getUser().getProfile().getPeriodMode();
        filterTiming.stop();

        List<Future<List<Statistic>>> statisticFutures = new ArrayList<>(12);

        // Expenses.
        statisticFutures.add(executor.submit(() ->
                applyResolutionTypesToFunction(Statistic.Types.EXPENSES_BY_CATEGORY, resolutionTypes ->
                                StatisticsGeneratorAggregator
                                        .aggregateUserTransactionStatistics(userData, Statistic.Types.EXPENSES_BY_CATEGORY,
                                                resolutionTypes, includedTransactionExpenses, transactionExpenses,
                                                StatisticsGeneratorFunctions.STATISTICS_SUM_FUNCTION,
                                                monthlyAdjustedPeriodizationFunction,
                                                StatisticsGeneratorFunctions.STATISTICS_GROUP_FUNCTION,
                                                StatisticsGeneratorFunctions.TRANSACTION_CATEGORY_FUNCTION),
                        monthlyType, ResolutionTypes.WEEKLY, ResolutionTypes.DAILY)));

        statisticFutures.add(executor.submit(() ->
                applyResolutionTypesToFunction(Statistic.Types.EXPENSES_COUNT_BY_CATEGORY, resolutionTypes ->
                                StatisticsGeneratorAggregator.aggregateUserTransactionStatistics(userData,
                                        Statistic.Types.EXPENSES_COUNT_BY_CATEGORY, resolutionTypes,
                                        includedTransactionExpenses, transactionExpenses,
                                        StatisticsGeneratorFunctions.STATISTICS_COUNT_FUNCTION,
                                        monthlyAdjustedPeriodizationFunction,
                                        StatisticsGeneratorFunctions.STATISTICS_GROUP_FUNCTION,
                                        StatisticsGeneratorFunctions.TRANSACTION_CATEGORY_FUNCTION),
                        monthlyType, ResolutionTypes.WEEKLY, ResolutionTypes.DAILY)));

        // Income.

        statisticFutures.add(executor.submit(() ->
                applyResolutionTypesToFunction(Statistic.Types.INCOME_BY_CATEGORY, resolutionTypes ->
                                StatisticsGeneratorAggregator.aggregateUserTransactionStatistics(userData,
                                        Statistic.Types.INCOME_BY_CATEGORY, resolutionTypes, includedTransactionIncome,
                                        transactionIncome,
                                        StatisticsGeneratorFunctions.STATISTICS_SUM_FUNCTION,
                                        monthlyAdjustedPeriodizationFunction,
                                        StatisticsGeneratorFunctions.STATISTICS_GROUP_FUNCTION,
                                        StatisticsGeneratorFunctions.TRANSACTION_CATEGORY_FUNCTION),
                        monthlyType)));

        // Net-income.

        statisticFutures.add(executor.submit(() ->
                applyResolutionTypesToFunction(Statistic.Types.INCOME_NET, resolutionTypes ->
                                StatisticsGeneratorAggregator
                                        .aggregateUserTransactionStatistics(userData, Statistic.Types.INCOME_NET,
                                                resolutionTypes, includedTransactionIncomeAndExpenses,
                                                transactionIncomeAndExpenses,
                                                StatisticsGeneratorFunctions.STATISTICS_SUM_FUNCTION,
                                                monthlyAdjustedPeriodizationFunction,
                                                StatisticsGeneratorFunctions.STATISTICS_GROUP_FUNCTION,
                                                StatisticsGeneratorFunctions.TRANSACTION_NET_INCOME_FUNCTION),
                        monthlyType)));

        statisticFutures.add(executor.submit(() ->
                applyResolutionTypesToFunction(Statistic.Types.INCOME_AND_EXPENSES, resolutionTypes ->
                                StatisticsGeneratorAggregator.aggregateUserTransactionStatistics(userData,
                                        Statistic.Types.INCOME_AND_EXPENSES, resolutionTypes,
                                        includedTransactionIncomeAndExpenses, transactionIncomeAndExpenses,
                                        StatisticsGeneratorFunctions.STATISTICS_SUM_FUNCTION,
                                        monthlyAdjustedPeriodizationFunction,
                                        StatisticsGeneratorFunctions.STATISTICS_GROUP_FUNCTION,
                                        StatisticsGeneratorFunctions.TRANSACTION_CATEGORY_TYPE_FUNCTION),
                        monthlyType, ResolutionTypes.DAILY)));

        // The income and expenses count is only used by the ABN AMRO monthly summary activity generator. To save some
        // CPU cycles, only calculate these statistics if running in the ABN AMRO cluster.

        if (Objects.equal(Cluster.ABNAMRO, cluster)) {
            statisticFutures.add(executor.submit(() ->
                    applyResolutionTypesToFunction(Statistic.Types.INCOME_AND_EXPENSES_COUNT, resolutionTypes ->
                                    StatisticsGeneratorAggregator.aggregateUserTransactionStatistics(userData,
                                            Statistic.Types.INCOME_AND_EXPENSES_COUNT, resolutionTypes,
                                            includedTransactionIncomeAndExpenses, transactionIncomeAndExpenses,
                                            StatisticsGeneratorFunctions.STATISTICS_COUNT_FUNCTION,
                                            monthlyAdjustedPeriodizationFunction,
                                            StatisticsGeneratorFunctions.STATISTICS_GROUP_FUNCTION,
                                            StatisticsGeneratorFunctions.TRANSACTION_CATEGORY_TYPE_FUNCTION),
                            monthlyType)));
        }

        // Savings

        // Cornwall is experimenting with trying to single out expenses in the SAVINGS category as a measure of how much
        // people save. Let's try this for a while and see if it's any good.

        statisticFutures.add(executor.submit(() ->
                applyResolutionTypesToFunction(Statistic.Types.LEFT_TO_SPEND, resolutionTypes ->
                                StatisticsGeneratorAggregator
                                        .aggregateUserTransactionStatistics(userData, Statistic.Types.LEFT_TO_SPEND,
                                                resolutionTypes, includedTransactionIncomeAndExpenses, transactionIncomeAndExpenses,
                                                new StatisticsLeftToSpendFunction(
                                                        resolutionTypes, userData.getUser()),
                                                monthlyAdjustedPeriodizationFunction,
                                                StatisticsGeneratorFunctions.STATISTICS_PERIOD_GROUP_FUNCTION,
                                                StatisticsGeneratorFunctions.TRANSACTION_DATE_FUNCTION),
                        monthlyType)));

        return statisticFutures;
    }

    @VisibleForTesting
    List<Statistic> generateStatisticsBasedStatistics(final UserData userData,
            final MonthlyAdjustedPeriodizationFunction monthlyAdjustedPeriodizationFunction, int maxAveragePeriods) {
        final ResolutionTypes monthlyType = userData.getUser().getProfile().getPeriodMode();

        return applyResolutionTypesToFunction(Statistic.Types.LEFT_TO_SPEND_AVERAGE, resolutionType ->
                        StatisticsGeneratorAggregator.aggregateStatisticsBasedStatistics(
                                userData,
                                Statistic.Types.LEFT_TO_SPEND_AVERAGE,
                                resolutionType,
                                monthlyAdjustedPeriodizationFunction,
                                new StatisticsLeftToSpendAverageFunction(resolutionType, userData.getUser(), maxAveragePeriods),
                                StatisticsGeneratorFunctions.STATISTICS_DAY_OF_MONTH_GROUP_FUNCTION),
                monthlyType);
    }

    private void populateUserDataWithLoanStatistics(final UserData userData) {
        final ResolutionTypes monthlyType = userData.getUser().getProfile().getPeriodMode();

        if (Objects.equal(Cluster.TINK, cluster)) {
            userData.addStatistics(applyResolutionTypesToFunction("loans",
                    resolutionType -> StatisticsGeneratorAggregator.aggregateLoanStatistics(userData, resolutionType),
                    monthlyType));
        }
    }

    private List<Statistic> applyResolutionTypesToFunction(String type, Function<ResolutionTypes,
            List<Statistic>> function, ResolutionTypes... types) {
        Timer.Context timing = time(type);
        List<Statistic> statistics = Arrays.stream(types).map(function)
                .flatMap(List::stream).collect(Collectors.toList());
        timing.stop();
        return statistics;
    }

    private Timer.Context time(String block) {
        return registry.timer(MetricId.newId("generate_statistic_types").label("block", block)).time();
    }

}
