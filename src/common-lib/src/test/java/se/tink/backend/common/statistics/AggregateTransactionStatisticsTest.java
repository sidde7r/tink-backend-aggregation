package se.tink.backend.common.statistics;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.common.statistics.factory.ExtendedPeriodFunctionFactory;
import se.tink.backend.common.statistics.factory.TransactionStatisticTransformerFactory;
import se.tink.backend.common.statistics.functions.MonthlyAdjustedPeriodizationFunction;
import se.tink.backend.common.statistics.functions.TransactionStatisticsTransformationFunction;
import se.tink.backend.common.workers.statistics.AggregateStatisticsRequest;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.exceptions.InvalidCandidateException;
import se.tink.backend.core.exceptions.TransactionNotFoundException;
import se.tink.backend.main.TestUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.backend.utils.TransactionPartUtils;

public class AggregateTransactionStatisticsTest {
    String DEFAULT_LOCALE = "sv_SE";

    Function<Transaction, Statistic> testTransformationFunction = new TransactionStatisticsTransformationFunction() {

        @Override
        public String description(Transaction t) {
            return t.getDescription();
        }
    };

    Function<Statistic, Integer> testGroupFunction = StatisticsGeneratorFunctions.STATISTICS_GROUP_FUNCTION;
    Function<Collection<Statistic>, Collection<Statistic>> testReducerFunction = StatisticsGeneratorFunctions.STATISTICS_SUM_FUNCTION;

    @Test(expected = NullPointerException.class)
    public void testNPEForNullTransformationFunction() {
        AggregateStatisticsRequest request = createTransactionRequest("test",
                createTransformerFactory(Lists.<Account>newArrayList(), "userId"), null,
                testGroupFunction, testReducerFunction, false);
        List<Transaction> filteredTransactions = Lists.newArrayList();
        List<Transaction> allTransactions = filteredTransactions;
        StatisticsGeneratorAggregator
                .aggregateTransactionStatistics(ResolutionTypes.MONTHLY, filteredTransactions, request,
                        "subject", allTransactions);
    }

    @Test(expected = NullPointerException.class)
    public void testAggregateTransactionStatisticsNullTransformerFactory() {
        AggregateStatisticsRequest request = createTransactionRequest("test", null,
                testTransformationFunction,
                testGroupFunction, testReducerFunction, false);
        List<Transaction> filteredTransactions = Lists.newArrayList();
        List<Transaction> allTransactions = filteredTransactions;
        StatisticsGeneratorAggregator
                .aggregateTransactionStatistics(ResolutionTypes.MONTHLY, filteredTransactions, request,
                        "subject", allTransactions);
    }

    @Test(expected = NullPointerException.class)
    public void testNPEForNullRequestType() {
        AggregateStatisticsRequest request = createTransactionRequest(null,
                createTransformerFactory(Lists.<Account>newArrayList(), "userId"),
                testTransformationFunction,
                testGroupFunction, testReducerFunction, false);
        List<Transaction> filteredTransactions = Lists.newArrayList();
        List<Transaction> allTransactions = filteredTransactions;
        StatisticsGeneratorAggregator
                .aggregateTransactionStatistics(ResolutionTypes.MONTHLY, filteredTransactions, request,
                        "subject", allTransactions);
    }

    @Test(expected = NullPointerException.class)
    public void testNPEForNullTransactions() {
        AggregateStatisticsRequest request = createTransactionRequest("test",
                createTransformerFactory(Lists.<Account>newArrayList(), "userId"),
                testTransformationFunction,
                testGroupFunction, testReducerFunction, false);
        List<Transaction> filteredTransactions = null;
        List<Transaction> allTransactions = filteredTransactions;
        StatisticsGeneratorAggregator
                .aggregateTransactionStatistics(ResolutionTypes.MONTHLY, filteredTransactions, request,
                        "subject", allTransactions);
    }

    @Test
    public void testEmptySizeForEmptyTransactions() {
        AggregateStatisticsRequest request = createTransactionRequest("test",
                createTransformerFactory(Lists.<Account>newArrayList(), "userId"),
                testTransformationFunction,
                testGroupFunction, testReducerFunction, false);
        List<Transaction> filteredTransactions = Lists.newArrayList();
        List<Transaction> allTransactions = filteredTransactions;
        List<Statistic> statistics = StatisticsGeneratorAggregator
                .aggregateTransactionStatistics(ResolutionTypes.MONTHLY, filteredTransactions, request,
                        "subject", allTransactions);

        int expStatisticSize = 0;

        Assert.assertEquals(expStatisticSize, statistics.size());
    }

    @Test(expected = NullPointerException.class)
    public void testNPEForWithoutGroupReduce() {
        AggregateStatisticsRequest request = createTransactionRequestWithoutGroupReduce("test",
                createTransformerFactory(Lists.<Account>newArrayList(), "userId"),
                testTransformationFunction, false);
        List<Transaction> filteredTransactions = null;
        List<Transaction> allTransactions = filteredTransactions;
        StatisticsGeneratorAggregator
                .aggregateTransactionStatistics(ResolutionTypes.MONTHLY, filteredTransactions, request,
                        "subject", allTransactions);
    }

    @Test
    public void testStatisticForOneTransaction() {
        ResolutionTypes resolution = ResolutionTypes.MONTHLY;
        String userId = "userId";
        String statisticType = "testAggregateTransactionStatisticsOneTransaction";

        AggregateStatisticsRequest request = createTransactionRequest(statisticType,
                createTransformerFactory(Lists.<Account>newArrayList(), userId),
                testTransformationFunction,
                testGroupFunction, testReducerFunction, false);
        List<Transaction> filteredTransactions = Lists.newArrayList(TestUtils.createTransaction(
                "Transaction X", -20, new Date(), userId, null, null));
        List<Transaction> allTransactions = filteredTransactions;

        List<Statistic> statistics = StatisticsGeneratorAggregator
                .aggregateTransactionStatistics(resolution, filteredTransactions, request,
                        "subject", allTransactions);

        int expStatisticSize = 1;
        double expTransactionAmount = -20.;

        Assert.assertEquals(expStatisticSize, statistics.size());
        Assert.assertEquals(statisticType, statistics.get(0).getType());
        Assert.assertEquals(resolution, statistics.get(0).getResolution());
        Assert.assertEquals(userId, statistics.get(0).getUserId());
        Assert.assertEquals(expTransactionAmount, statistics.get(0).getValue(), 0.001);
    }

    @Test
    public void testStatisticForByOneStatistic() {
        ResolutionTypes resolution = ResolutionTypes.MONTHLY;
        String userId = "userId";
        String statisticType = "testAggregateTransactionStatisticsByOneStatistic";

        AggregateStatisticsRequest request = createTransactionRequest(statisticType,
                createTransformerFactory(Lists.<Account>newArrayList(), userId),
                testTransformationFunction,
                testGroupFunction, testReducerFunction, false);

        List<Transaction> filteredTransactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, new Date(), userId, null, null),
                TestUtils.createTransaction("Transaction X", -30, new Date(), userId, null, null),
                TestUtils.createTransaction("Transaction X", -50, new Date(), userId, null, null));

        List<Transaction> allTransactions = filteredTransactions;

        List<Statistic> statistics = StatisticsGeneratorAggregator
                .aggregateTransactionStatistics(resolution, filteredTransactions, request,
                        "subject", allTransactions);

        int expStatisticSize = 1;
        double expTransactionAmount = -100.;

        Assert.assertEquals(expStatisticSize, statistics.size());
        Assert.assertEquals(statisticType, statistics.get(0).getType());
        Assert.assertEquals(resolution, statistics.get(0).getResolution());
        Assert.assertEquals(userId, statistics.get(0).getUserId());
        Assert.assertEquals(expTransactionAmount, statistics.get(0).getValue(), 0.001);
    }

    @Test
    public void testStatisticForTwoStatisticDifferentDescription() {
        ResolutionTypes resolution = ResolutionTypes.MONTHLY;
        String userId = "userId";
        String statisticType = "testAggregateTransactionStatisticsByTwoStatisticDifferentDescription";

        AggregateStatisticsRequest request = createTransactionRequest(statisticType,
                createTransformerFactory(Lists.<Account>newArrayList(), userId),
                testTransformationFunction,
                testGroupFunction, testReducerFunction, false);

        List<Transaction> filteredTransactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, new Date(), userId, null, null),
                TestUtils.createTransaction("Transaction X", -30, new Date(), userId, null, null),
                TestUtils.createTransaction("Transaction Y", -50, new Date(), userId, null, null));

        List<Transaction> allTransactions = filteredTransactions;

        List<Statistic> statistics = StatisticsGeneratorAggregator
                .aggregateTransactionStatistics(resolution, filteredTransactions, request,
                        "subject", allTransactions);

        int expStatisticSize = 2;
        double expTransactionAmount = -50.;

        Assert.assertEquals(expStatisticSize, statistics.size());

        for (Statistic statistic : statistics) {
            Assert.assertEquals(statisticType, statistic.getType());
            Assert.assertEquals(resolution, statistic.getResolution());
            Assert.assertEquals(userId, statistic.getUserId());
            Assert.assertEquals(expTransactionAmount, statistic.getValue(), 0.001);
        }

    }

    @Test
    public void testStatisticForTwoStatisticDifferentMonth() {
        ResolutionTypes resolution = ResolutionTypes.MONTHLY;
        String userId = "userId";
        String statisticType = "testAggregateTransactionStatisticsByTwoStatisticDifferentDescription";

        AggregateStatisticsRequest request = createTransactionRequest(statisticType,
                createTransformerFactory(Lists.<Account>newArrayList(), userId),
                testTransformationFunction,
                testGroupFunction, testReducerFunction, false);

        List<Transaction> filteredTransactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, new Date(), userId, null, null),
                TestUtils.createTransaction("Transaction X", -30, new Date(), userId, null, null),
                TestUtils.createTransaction("Transaction X", -50, DateUtils.addMonths(new Date(), -1), userId, null,
                        null));

        List<Transaction> allTransactions = filteredTransactions;

        List<Statistic> statistics = StatisticsGeneratorAggregator
                .aggregateTransactionStatistics(resolution, filteredTransactions, request,
                        "subject", allTransactions);

        int expStatisticSize = 2;
        double expTransactionAmount = -50.;

        Assert.assertEquals(expStatisticSize, statistics.size());

        for (Statistic statistic : statistics) {
            Assert.assertEquals(statisticType, statistic.getType());
            Assert.assertEquals(resolution, statistic.getResolution());
            Assert.assertEquals(userId, statistic.getUserId());
            Assert.assertEquals(expTransactionAmount, statistic.getValue(), 0.001);
        }

    }

    @Test
    public void testStatisticForDisregardAccountOwnership() {
        UserProfile profile = new UserProfile();
        profile.setLocale(DEFAULT_LOCALE);

        User user = TestUtils.createUser("test");
        user.setProfile(profile);

        List<Transaction> transactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, new Date(), user.getId(), null, null),
                TestUtils.createTransaction("Transaction X", -30, new Date(), user.getId(), null, null),
                TestUtils.createTransaction("Transaction X", -50, new Date(), user.getId(), null, null));

        UserData userData = TestUtils.createUserDate(user, Lists.newArrayList(TestUtils.createAccount(2000, false)),
                Lists.newArrayList(TestUtils.createCregentials()), transactions);

        String statisticType = "testAggregateTransactionStatisticsByOneStatistic";
        ResolutionTypes resolution = ResolutionTypes.MONTHLY;

        List<Statistic> statistics = StatisticsGeneratorAggregator
                .aggregateUserTransactionStatistics(userData, statisticType, resolution, userData.getTransactions(),
                        userData.getTransactions(),
                        testReducerFunction, new MonthlyAdjustedPeriodizationFunction(0),
                        testGroupFunction, true, testTransformationFunction);

        int expStatisticSize = 1;
        double expTransactionAmount = -100.;

        Assert.assertEquals(expStatisticSize, statistics.size());
        Assert.assertEquals(statisticType, statistics.get(0).getType());
        Assert.assertEquals(resolution, statistics.get(0).getResolution());
        Assert.assertEquals(user.getId(), statistics.get(0).getUserId());
        Assert.assertEquals(expTransactionAmount, statistics.get(0).getValue(), 0.001);
    }

    @Test
    public void testStatisticForNotDisregardAccountOwnership() {
        UserProfile profile = new UserProfile();
        profile.setLocale(DEFAULT_LOCALE);

        User user = TestUtils.createUser("test");
        user.setProfile(profile);

        List<Transaction> transactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, new Date(), user.getId(), null, null),
                TestUtils.createTransaction("Transaction X", -30, new Date(), user.getId(), null, null),
                TestUtils.createTransaction("Transaction X", -50, new Date(), user.getId(), null, null));

        UserData userData = TestUtils.createUserDate(user, Lists.newArrayList(TestUtils.createAccount(2000, false)),
                Lists.newArrayList(TestUtils.createCregentials()), transactions);

        String statisticType = "testAggregateTransactionStatisticsByOneStatistic";
        ResolutionTypes resolution = ResolutionTypes.MONTHLY;

        List<Statistic> statistics = StatisticsGeneratorAggregator
                .aggregateUserTransactionStatistics(userData, statisticType, resolution, userData.getTransactions(),
                        userData.getTransactions(),
                        testReducerFunction, new MonthlyAdjustedPeriodizationFunction(0),
                        testGroupFunction, false, testTransformationFunction);

        int expStatisticSize = 1;
        double expTransactionAmount = -100.;

        Assert.assertEquals(expStatisticSize, statistics.size());
        Assert.assertEquals(statisticType, statistics.get(0).getType());
        Assert.assertEquals(resolution, statistics.get(0).getResolution());
        Assert.assertEquals(user.getId(), statistics.get(0).getUserId());
        Assert.assertEquals(expTransactionAmount, statistics.get(0).getValue(), 0.001);
    }

    @Test
    public void testStatisticForTransactionParts() throws InvalidCandidateException, TransactionNotFoundException {
        ResolutionTypes resolution = ResolutionTypes.MONTHLY;
        String userId = "userId";
        String statisticType = "sd";
        Category excludedCategory = buildRandomCategory(CategoryTypes.TRANSFERS);
        Category foodCategory = buildRandomCategory(CategoryTypes.EXPENSES);
        Category incomeCategory = buildRandomCategory(CategoryTypes.INCOME);

        AggregateStatisticsRequest request = createTransactionRequest(statisticType,
                createTransformerFactory(Lists.newArrayList(), userId),
                testTransformationFunction,
                testGroupFunction, testReducerFunction, false);

        Transaction expense = TestUtils.createTransaction("Reload Food", -100, new Date(), userId, foodCategory, null);
        Transaction income = TestUtils.createTransaction("Swish", 25, new Date(), userId, incomeCategory, null);

        TransactionPartUtils.link(expense, income, excludedCategory);

        List<Transaction> filteredTransactions = Lists.newArrayList(expense, income);

        List<Transaction> allTransactions = filteredTransactions;

        List<Statistic> statistics = StatisticsGeneratorAggregator
                .aggregateTransactionStatistics(resolution, filteredTransactions, request,
                        "subject", allTransactions);

        int expStatisticSize = 1;
        double expTransactionAmount = -75;

        Assert.assertEquals(expStatisticSize, statistics.size());
        Assert.assertEquals(statisticType, statistics.get(0).getType());
        Assert.assertEquals(resolution, statistics.get(0).getResolution());
        Assert.assertEquals(userId, statistics.get(0).getUserId());
        Assert.assertEquals(expTransactionAmount, statistics.get(0).getValue(), 0.001);
    }

    private static Category buildRandomCategory(CategoryTypes type) {
        Category category = new Category();
        category.setId(StringUtils.generateUUID());
        category.setType(type);
        return category;
    }

    private AggregateStatisticsRequest createTransactionRequestWithoutGroupReduce(String type,
            TransactionStatisticTransformerFactory transformerFactory,
            Function<Transaction, Statistic> transformationFunction, boolean disregardAccountOwnership) {
        return new AggregateStatisticsRequest(type)
                .setTransformerFactory(transformerFactory)
                .setTransformationFunction(transformationFunction)
                .setDisregardAccountOwnership(disregardAccountOwnership);
    }

    private AggregateStatisticsRequest createTransactionRequest(String type,
            TransactionStatisticTransformerFactory transformerFactory,
            Function<Transaction, Statistic> transformationFunction, Function<Statistic, Integer> groupFunction,
            Function<Collection<Statistic>, Collection<Statistic>> reducerFunction, boolean disregardAccountOwnership) {
        return new AggregateStatisticsRequest(type)
                .setTransformerFactory(transformerFactory)
                .setTransformationFunction(transformationFunction).addGroupReduce(groupFunction, reducerFunction)
                .setDisregardAccountOwnership(disregardAccountOwnership);
    }

    private TransactionStatisticTransformerFactory createTransformerFactory(List<Account> accounts, String userId) {
        return new TransactionStatisticTransformerFactory(
                accounts,
                new ExtendedPeriodFunctionFactory(DEFAULT_LOCALE, new MonthlyAdjustedPeriodizationFunction(0)), userId);
    }

}
