package se.tink.backend.common.statistics;

import com.google.common.collect.Lists;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import se.tink.backend.common.SwedishTimeRule;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.common.statistics.functions.MonthlyAdjustedPeriodizationFunction;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountBalance;
import se.tink.backend.core.Category;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.main.TestUtils;
import se.tink.libraries.metrics.MetricRegistry;

public class StatisticsGeneratorExpensesCountByCategoryTest extends StatisticGeneratorUnitTest {
    private StatisticsGenerator statisticsGenerator;
    private final static String statisticType = Statistic.Types.EXPENSES_COUNT_BY_CATEGORY;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
    Date testDate;

    @Rule
    public SwedishTimeRule timeRule = new SwedishTimeRule();

    @Before
    public void setUp() throws ParseException {
        List<Category> categories = generateCategories();
        statisticsGenerator = new StatisticsGenerator(categories, new SECategories(), new MetricRegistry(),
                Cluster.TINK);
        testDate = formatter.parse("2016/09/21");
    }

    @Test
    public void testAllTransactionsExcluded() {
        User user = TestUtils.createUser("test");
        Account account = TestUtils.createAccount(2000, true);

        List<Transaction> transactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction 1", -20, testDate, user.getId(), excludeCategories.get(0),
                        account.getId()),
                TestUtils.createTransaction("Transaction 2", -30, testDate, user.getId(), excludeCategories.get(0),
                        account.getId()),
                TestUtils.createTransaction("Transaction 3", -50, testDate, user.getId(), excludeCategories.get(0),
                        account.getId()));
        UserData userData = createUserDate(user, transactions, account);

        List<Statistic> statistics = unwrapAndFilter(statisticType, statisticsGenerator
                .transactionStatistics(userData, new MonthlyAdjustedPeriodizationFunction(0)));

        int expSize = 3;
        Assert.assertEquals(expSize, statistics.size());

        assertValue(statistics, 3, 3, 3, 3, 3);
        assertResolutionTypeToSize(statistics, 1, 1, 0, 1, 0);
    }

    @Test
    public void testSameCategory() {
        User user = TestUtils.createUser("test");
        Account account = TestUtils.createAccount(2000, false);

        List<Transaction> transactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, testDate, user.getId(), expensesCategories.get(0),
                        account.getId()),
                TestUtils.createTransaction("Transaction X", -30, testDate, user.getId(), expensesCategories.get(0),
                        account.getId()),
                TestUtils.createTransaction("Transaction X", -50, testDate, user.getId(), expensesCategories.get(0),
                        account.getId()));

        UserData userData = createUserDate(user, transactions, account);

        List<Statistic> statistics = unwrapAndFilter(statisticType, statisticsGenerator
                .transactionStatistics(userData, new MonthlyAdjustedPeriodizationFunction(0)));

        int expSize = 3;
        Assert.assertEquals(expSize, statistics.size());

        assertValue(statistics, 3, 3, 3, 3, 0);
        assertResolutionTypeToSize(statistics, 1, 1, 0, 1, 0);
    }

    @Test
    public void testSameCategoryTwoDays() {
        User user = TestUtils.createUser("test");
        Account account = TestUtils.createAccount(2000, false);

        List<Transaction> transactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, testDate, user.getId(), expensesCategories.get(0),
                        account.getId()),
                TestUtils.createTransaction("Transaction X", -30, testDate, user.getId(), expensesCategories.get(0),
                        account.getId()),
                TestUtils.createTransaction("Transaction Y", -50, DateUtils.addDays(testDate, -1), user.getId(),
                        expensesCategories.get(0), account.getId()),
                TestUtils.createTransaction("Transaction Y", -50, DateUtils.addDays(testDate, -1), user.getId(),
                        expensesCategories.get(0), account.getId()));

        UserData userData = createUserDate(user, transactions, account);

        List<Statistic> statistics = unwrapAndFilter(statisticType, statisticsGenerator
                .transactionStatistics(userData, new MonthlyAdjustedPeriodizationFunction(0)));

        int expSize = 4;
        assertSizeFormated("", statistics.size(), expSize);
        assertValue(statistics, 2, 4, 4, 4, 0);
        assertResolutionTypeToSize(statistics, 2, 1, 0, 1, 0);
    }

    @Test
    public void testSameCategoryTwoWeeks() {
        User user = TestUtils.createUser("test");
        Account account = TestUtils.createAccount(2000, false);

        List<Transaction> transactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, testDate, user.getId(), expensesCategories.get(0),
                        account.getId()),
                TestUtils.createTransaction("Transaction X", -30, testDate, user.getId(), expensesCategories.get(0),
                        account.getId()),
                TestUtils.createTransaction("Transaction Y", -50, DateUtils.addWeeks(testDate, -1), user.getId(),
                        expensesCategories.get(0), account.getId()),
                TestUtils.createTransaction("Transaction Y", -50, DateUtils.addWeeks(testDate, -1), user.getId(),
                        expensesCategories.get(0), account.getId()));

        UserData userData = createUserDate(user, transactions, account);

        List<Statistic> statistics = unwrapAndFilter(statisticType, statisticsGenerator
                .transactionStatistics(userData, new MonthlyAdjustedPeriodizationFunction(0)));

        int expSize = 5;
        assertSizeFormated("", statistics.size(), expSize);
        assertValue(statistics, 2, 2, 4, 4, 0);
        assertResolutionTypeToSize(statistics, 2, 2, 0, 1, 0);
    }

    @Test
    public void testSameCategoryTwoMonths() {
        User user = TestUtils.createUser("test");
        Account account = TestUtils.createAccount(2000, false);

        List<Transaction> transactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, testDate, user.getId(), expensesCategories.get(0),
                        account.getId()),
                TestUtils.createTransaction("Transaction X", -30, testDate, user.getId(), expensesCategories.get(0),
                        account.getId()),
                TestUtils.createTransaction("Transaction Y", -50, DateUtils.addMonths(testDate, -1), user.getId(),
                        expensesCategories.get(0), account.getId()),
                TestUtils.createTransaction("Transaction Y", -50, DateUtils.addMonths(testDate, -1), user.getId(),
                        expensesCategories.get(0), account.getId()));

        UserData userData = createUserDate(user, transactions, account);

        List<Statistic> statistics = unwrapAndFilter(statisticType, statisticsGenerator
                .transactionStatistics(userData, new MonthlyAdjustedPeriodizationFunction(0)));

        int expSize = 6;
        assertSizeFormated("", statistics.size(), expSize);
        assertValue(statistics, 2, 2, 2, 2, 0);
        assertResolutionTypeToSize(statistics, 2, 2, 0, 2, 0);
    }

    @Test
    public void testSameCategoryTwoYears() {
        User user = TestUtils.createUser("test");
        Account account = TestUtils.createAccount(2000, false);

        List<Transaction> transactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, testDate, user.getId(), expensesCategories.get(0),
                        account.getId()),
                TestUtils.createTransaction("Transaction X", -30, testDate, user.getId(), expensesCategories.get(0),
                        account.getId()),
                TestUtils.createTransaction("Transaction Y", -50, DateUtils.addYears(testDate, -1), user.getId(),
                        expensesCategories.get(0), account.getId()),
                TestUtils.createTransaction("Transaction Y", -50, DateUtils.addYears(testDate, -1), user.getId(),
                        expensesCategories.get(0), account.getId()));

        UserData userData = createUserDate(user, transactions, account);

        List<Statistic> statistics = unwrapAndFilter(statisticType, statisticsGenerator
                .transactionStatistics(userData, new MonthlyAdjustedPeriodizationFunction(0)));

        int expSize = 6;
        assertSizeFormated("", statistics.size(), expSize);
        assertValue(statistics, 2, 2, 2, 2, 0);
        assertResolutionTypeToSize(statistics, 2, 2, 0, 2, 0);
    }

    @Test
    public void testDifferentCategory() {
        User user = TestUtils.createUser("test");
        Account account = TestUtils.createAccount(2000, false);

        List<Transaction> transactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, testDate, user.getId(), expensesCategories.get(0),
                        account.getId()),
                TestUtils.createTransaction("Transaction X", -30, testDate, user.getId(), expensesCategories.get(0),
                        account.getId()),
                TestUtils.createTransaction("Transaction Y", -50, testDate, user.getId(),
                        expensesCategories.get(1), account.getId()),
                TestUtils.createTransaction("Transaction Y", -50, testDate, user.getId(),
                        expensesCategories.get(1), account.getId()));

        UserData userData = createUserDate(user, transactions, account);

        List<Statistic> statistics = unwrapAndFilter(statisticType, statisticsGenerator
                .transactionStatistics(userData, new MonthlyAdjustedPeriodizationFunction(0)));

        int expSize = 6;
        assertSizeFormated("", statistics.size(), expSize);
        assertValue(statistics, 2, 2, 2, 2, 0);
        assertResolutionTypeToSize(statistics, 2, 2, 0, 2, 0);
    }

    @Test
    public void testDifferentCategoryTwoDays() {
        User user = TestUtils.createUser("test");
        Account account = TestUtils.createAccount(2000, false);

        List<Transaction> transactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, testDate, user.getId(), expensesCategories.get(0),
                        account.getId()),
                TestUtils.createTransaction("Transaction X", -50, DateUtils.addDays(testDate, -1), user.getId(),
                        expensesCategories.get(0), account.getId()),
                TestUtils.createTransaction("Transaction Y", -30, testDate, user.getId(), expensesCategories.get(1),
                        account.getId()),
                TestUtils.createTransaction("Transaction Y", -50, DateUtils.addDays(testDate, -1), user.getId(),
                        expensesCategories.get(1), account.getId()));

        UserData userData = createUserDate(user, transactions, account);

        List<Statistic> statistics = unwrapAndFilter(statisticType, statisticsGenerator
                .transactionStatistics(userData, new MonthlyAdjustedPeriodizationFunction(0)));

        int expSize = 8;
        assertSizeFormated("", statistics.size(), expSize);
        assertValue(statistics, 1, 2, 2, 2, 0);
        assertResolutionTypeToSize(statistics, 4, 2, 0, 2, 0);
    }

    @Test
    public void testDifferentCategoryTwoWeeks() {
        User user = TestUtils.createUser("test");
        Account account = TestUtils.createAccount(2000, false);

        List<Transaction> transactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, testDate, user.getId(), expensesCategories.get(0),
                        account.getId()),
                TestUtils.createTransaction("Transaction X", -50, DateUtils.addWeeks(testDate, -1), user.getId(),
                        expensesCategories.get(0), account.getId()),
                TestUtils.createTransaction("Transaction Y", -20, testDate, user.getId(), expensesCategories.get(1),
                        account.getId()),
                TestUtils.createTransaction("Transaction Y", -50, DateUtils.addWeeks(testDate, -1), user.getId(),
                        expensesCategories.get(1), account.getId()));

        UserData userData = createUserDate(user, transactions, account);

        List<Statistic> statistics = unwrapAndFilter(statisticType, statisticsGenerator
                .transactionStatistics(userData, new MonthlyAdjustedPeriodizationFunction(0)));

        int expSize = 10;
        assertSizeFormated("", statistics.size(), expSize);
        assertValue(statistics, 1, 1, 2, 2, 0);
        assertResolutionTypeToSize(statistics, 4, 4, 0, 2, 0);
    }

    @Test
    public void testDifferentCategoryTwoMonths() {
        User user = TestUtils.createUser("test");
        Account account = TestUtils.createAccount(2000, false);

        List<Transaction> transactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, testDate, user.getId(), expensesCategories.get(0),
                        account.getId()),
                TestUtils.createTransaction("Transaction X", -50, DateUtils.addMonths(testDate, -1), user.getId(),
                        expensesCategories.get(0), account.getId()),
                TestUtils.createTransaction("Transaction Y", -20, testDate, user.getId(), expensesCategories.get(1),
                        account.getId()),
                TestUtils.createTransaction("Transaction Y", -50, DateUtils.addMonths(testDate, -1), user.getId(),
                        expensesCategories.get(1), account.getId()));

        UserData userData = createUserDate(user, transactions, account);

        List<Statistic> statistics = unwrapAndFilter(statisticType, statisticsGenerator
                .transactionStatistics(userData, new MonthlyAdjustedPeriodizationFunction(0)));

        int expSize = 12;
        assertSizeFormated("", statistics.size(), expSize);
        assertValue(statistics, 1, 1, 1, 1, 0);
        assertResolutionTypeToSize(statistics, 4, 4, 0, 4, 0);
    }

    @Test
    public void testDifferentCategoryTwoYears() {
        User user = TestUtils.createUser("test");
        Account account = TestUtils.createAccount(2000, false);

        List<Transaction> transactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, testDate, user.getId(), expensesCategories.get(0),
                        account.getId()),
                TestUtils.createTransaction("Transaction X", -50, DateUtils.addYears(testDate, -1), user.getId(),
                        expensesCategories.get(0), account.getId()),
                TestUtils.createTransaction("Transaction Y", -20, testDate, user.getId(), expensesCategories.get(1),
                        account.getId()),
                TestUtils.createTransaction("Transaction Y", -50, DateUtils.addYears(testDate, -1), user.getId(),
                        expensesCategories.get(1), account.getId()));

        UserData userData = createUserDate(user, transactions, account);

        List<Statistic> statistics = unwrapAndFilter(statisticType, statisticsGenerator
                .transactionStatistics(userData, new MonthlyAdjustedPeriodizationFunction(0)));

        int expSize = 12;
        assertSizeFormated("", statistics.size(), expSize);
        assertValue(statistics, 1, 1, 1, 1, 0);
        assertResolutionTypeToSize(statistics, 4, 4, 0, 4, 0);
    }

    @Test
    public void testIncomeCategoryType() {
        User user = TestUtils.createUser("test");
        Account account = TestUtils.createAccount(2000, false);

        List<Transaction> transactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, testDate, user.getId(), incomeCategories.get(0),
                        account.getId()),
                TestUtils.createTransaction("Transaction X", -30, testDate, user.getId(), incomeCategories.get(0),
                        account.getId()),
                TestUtils.createTransaction("Transaction X", -50, testDate, user.getId(), incomeCategories.get(0),
                        account.getId()));

        UserData userData = createUserDate(user, transactions, account);

        List<Statistic> statistics = unwrapAndFilter(statisticType, statisticsGenerator
                .transactionStatistics(userData, new MonthlyAdjustedPeriodizationFunction(0)));

        int expSize = 0;
        Assert.assertEquals(expSize, statistics.size());
    }

    @Test
    public void testExcludeCategory() {
        User user = TestUtils.createUser("test");
        Account account = TestUtils.createAccount(2000, false);

        List<Transaction> transactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, testDate, user.getId(), excludeCategories.get(1),
                        account.getId()),
                TestUtils.createTransaction("Transaction X", -30, testDate, user.getId(), excludeCategories.get(1),
                        account.getId()),
                TestUtils.createTransaction("Transaction X", -50, testDate, user.getId(), excludeCategories.get(1),
                        account.getId()));

        UserData userData = createUserDate(user, transactions, account);

        List<Statistic> statistics = unwrapAndFilter(statisticType, statisticsGenerator
                .transactionStatistics(userData, new MonthlyAdjustedPeriodizationFunction(0)));

        int expSize = 0;
        Assert.assertEquals(expSize, statistics.size());
    }

    @Test
    public void testExcludeAccount() {
        User user = TestUtils.createUser("test");
        Account account = TestUtils.createAccount(2000, true);

        List<Transaction> transactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, testDate, user.getId(), expensesCategories.get(0),
                        account.getId()),
                TestUtils.createTransaction("Transaction X", -30, testDate, user.getId(), expensesCategories.get(0),
                        account.getId()),
                TestUtils.createTransaction("Transaction X", -50, testDate, user.getId(), expensesCategories.get(0),
                        account.getId()));

        UserData userData = TestUtils.createUserData(user, Lists.newArrayList(account),
                Lists.newArrayList(TestUtils.createCregentials()), transactions,
                Lists.<AccountBalance>newArrayList());

        List<Statistic> statistics = unwrapAndFilter(statisticType, statisticsGenerator
                .transactionStatistics(userData, new MonthlyAdjustedPeriodizationFunction(0)));

        int expSize = 3;
        Assert.assertEquals(expSize, statistics.size());
    }

    private UserData createUserDate(User user, List<Transaction> transactions, Account account) {

        return TestUtils.createUserData(user, Lists.newArrayList(account),
                Lists.newArrayList(TestUtils.createCregentials()), transactions,
                Lists.<AccountBalance>newArrayList());
    }
}
