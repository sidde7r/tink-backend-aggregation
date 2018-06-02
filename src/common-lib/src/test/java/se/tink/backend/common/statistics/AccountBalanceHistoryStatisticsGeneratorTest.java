package se.tink.backend.common.statistics;

import com.google.common.collect.Lists;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.common.statistics.functions.MonthlyAdjustedPeriodizationFunction;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountBalance;
import se.tink.backend.core.Category;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.main.TestUtils;
import se.tink.libraries.metrics.MetricRegistry;

public class AccountBalanceHistoryStatisticsGeneratorTest extends StatisticGeneratorUnitTest{
    private StatisticsGenerator statisticsGenerator;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
    Date testDate;

    @Before
    public void setUp() throws ParseException {
        List<Category> categories = generateCategories();
        statisticsGenerator = new StatisticsGenerator(categories,
                new SECategories(), new MetricRegistry(), Cluster.TINK);
        testDate = formatter.parse("2016/09/22");
    }

    @Test
    public void testAccountBalanceHistory() {
        User user = TestUtils.createUser("test");

        List<Transaction> transactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, testDate, user.getId(), null, null),
                TestUtils.createTransaction("Transaction X", -30, testDate, user.getId(), null, null),
                TestUtils.createTransaction("Transaction X", -50, testDate, user.getId(), null, null));

        Account account = TestUtils.createAccount(2000, false);
        List<Account> accounts = Lists.newArrayList(account);
        List<Credentials> credentials = Lists.newArrayList(TestUtils.createCregentials());
        List<AccountBalance> accountBalanceHistory = Lists.newArrayList(
                TestUtils.createAccountBalanceEntry(account, testDate, 10000));

        UserData userData = TestUtils.createUserData(user, accounts, credentials, transactions, accountBalanceHistory);

        List<Statistic> statistics = unwrap(statisticsGenerator
                .accountHistoryStatistics(userData, new MonthlyAdjustedPeriodizationFunction(0)));

        int expSize = 4;
        Assert.assertEquals(expSize, statistics.size());

        assertResolutionTypeToSize(statistics, 2, 1, 0, 1, 0);
    }

    @Test
    public void testAccountBalanceHistoryTwoDays() {
        User user = TestUtils.createUser("test");

        List<Transaction> transactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, testDate, user.getId(), null, null),
                TestUtils.createTransaction("Transaction X", -30, testDate, user.getId(), null, null),
                TestUtils.createTransaction("Transaction X", -50, testDate, user.getId(), null, null));

        Account account = TestUtils.createAccount(2000, false);
        List<Account> accounts = Lists.newArrayList(account);
        List<Credentials> credentials = Lists.newArrayList(TestUtils.createCregentials());
        List<AccountBalance> accountBalanceHistory = Lists.newArrayList(
                TestUtils.createAccountBalanceEntry(account, testDate, 100),
                TestUtils.createAccountBalanceEntry(account, DateUtils.addDays(testDate, -1), 1000));
        
        UserData userData = TestUtils.createUserData(user, accounts, credentials, transactions, accountBalanceHistory);

        List<Statistic> statistics = unwrap(statisticsGenerator
                .accountHistoryStatistics(userData, new MonthlyAdjustedPeriodizationFunction(0)));

        int expSize = 6;

        assertSizeFormated("", statistics.size(), expSize);
        assertResolutionTypeToSize(statistics, 4, 1, 0, 1, 0);
    }

    @Test
    public void testAccountBalanceHistoryTwoWeeks() {
        User user = TestUtils.createUser("test");

        List<Transaction> transactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, testDate, user.getId(), null, null),
                TestUtils.createTransaction("Transaction X", -30, testDate, user.getId(), null, null),
                TestUtils.createTransaction("Transaction X", -50, testDate, user.getId(), null, null));

        Account account = TestUtils.createAccount(2000, false);
        List<Account> accounts = Lists.newArrayList(account);
        List<Credentials> credentials = Lists.newArrayList(TestUtils.createCregentials());
        List<AccountBalance> accountBalanceHistory = Lists.newArrayList(
                TestUtils.createAccountBalanceEntry(account, testDate, 100),
                TestUtils.createAccountBalanceEntry(account, DateUtils.addWeeks(testDate, -1), 1000));
        
        UserData userData = TestUtils.createUserData(user, accounts, credentials, transactions, accountBalanceHistory);

        List<Statistic> statistics = unwrap(statisticsGenerator
                .accountHistoryStatistics(userData, new MonthlyAdjustedPeriodizationFunction(0)));

        int expSize = 19;

        assertSizeFormated("", statistics.size(), expSize);
        assertResolutionTypeToSize(statistics, 16, 2, 0, 1, 0);
    }

    @Test
    public void testAccountBalanceHistoryTwoMonths() {
        User user = TestUtils.createUser("test");

        List<Transaction> transactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, testDate, user.getId(), null, null),
                TestUtils.createTransaction("Transaction X", -30, testDate, user.getId(), null, null),
                TestUtils.createTransaction("Transaction X", -50, testDate, user.getId(), null, null));

        Account account = TestUtils.createAccount(2000, false);
        List<Account> accounts = Lists.newArrayList(account);
        List<Credentials> credentials = Lists.newArrayList(TestUtils.createCregentials());
        List<AccountBalance> accountBalanceHistory = Lists.newArrayList(
                TestUtils.createAccountBalanceEntry(account, testDate, 100),
                TestUtils.createAccountBalanceEntry(account, DateUtils.addMonths(testDate, -1), 1000));
        
        UserData userData = TestUtils.createUserData(user, accounts, credentials, transactions, accountBalanceHistory);

        List<Statistic> statistics = unwrap(statisticsGenerator
                .accountHistoryStatistics(userData, new MonthlyAdjustedPeriodizationFunction(0)));

        int expSize = 71;

        assertSizeFormated("", statistics.size(), expSize);
        assertResolutionTypeToSize(statistics, 64, 5, 0, 2, 0);
    }

    @Test
    public void testAccountBalanceHistoryTwoYears() {
        User user = TestUtils.createUser("test");

        List<Transaction> transactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, testDate, user.getId(), null, null),
                TestUtils.createTransaction("Transaction X", -30, testDate, user.getId(), null, null),
                TestUtils.createTransaction("Transaction X", -50, testDate, user.getId(), null, null));

        Account account = TestUtils.createAccount(2000, false);
        List<Account> accounts = Lists.newArrayList(account);
        List<Credentials> credentials = Lists.newArrayList(TestUtils.createCregentials());
        List<AccountBalance> accountBalanceHistory = Lists.newArrayList(
                TestUtils.createAccountBalanceEntry(account, testDate, 100),
                TestUtils.createAccountBalanceEntry(account, DateUtils.addYears(testDate, -1), 1000));
        
        UserData userData = TestUtils.createUserData(user, accounts, credentials, transactions, accountBalanceHistory);

        List<Statistic> statistics = unwrap(statisticsGenerator
                .accountHistoryStatistics(userData, new MonthlyAdjustedPeriodizationFunction(0)));

        int expSize = 800;

        assertSizeFormated("", statistics.size(), expSize);
        assertResolutionTypeToSize(statistics, 734, 53, 0, 13, 0);
    }

    @Test
    public void testEmptyHistory() {
        User user = TestUtils.createUser("test");

        List<Transaction> transactions = Lists.newArrayList(
                TestUtils.createTransaction("Transaction X", -20, testDate, user.getId(), null, null),
                TestUtils.createTransaction("Transaction X", -30, testDate, user.getId(), null, null),
                TestUtils.createTransaction("Transaction X", -50, testDate, user.getId(), null, null));

        Account account = TestUtils.createAccount(2000, false);
        List<Account> accounts = Lists.newArrayList(account);
        List<Credentials> credentials = Lists.newArrayList(TestUtils.createCregentials());
        List<AccountBalance> accountBalanceHistory = Lists.newArrayList();
        
        UserData userData = TestUtils.createUserData(user, accounts, credentials, transactions, accountBalanceHistory);

        List<Statistic> statistics = unwrap(statisticsGenerator
                .accountHistoryStatistics(userData, new MonthlyAdjustedPeriodizationFunction(0)));

        int expSize = 0;
        Assert.assertEquals(expSize, statistics.size());
    }

}
