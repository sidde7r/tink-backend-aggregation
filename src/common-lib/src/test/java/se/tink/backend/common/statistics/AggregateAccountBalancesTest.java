package se.tink.backend.common.statistics;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.common.statistics.functions.AccountBalanceToAccountTypeFunction;
import se.tink.backend.common.statistics.functions.MonthlyAdjustedPeriodizationFunction;
import se.tink.backend.common.statistics.functions.StatisticsSumming;
import se.tink.backend.common.utils.AccountBalanceUtils;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountBalance;
import se.tink.backend.core.AccountTypes;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.backend.core.UserProfile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AggregateAccountBalancesTest {

    private UserData userData;
    private List<AccountBalance> accountBalanceHistory;
    private Date testDate;
    private MonthlyAdjustedPeriodizationFunction monthlyAdjustedPeriodizationFunction;
    private ArrayList<Account> savingAccounts;
    private ArrayList<Account> cardsAndAccountsAccounts;
    private ArrayList<Account> loanAccounts;
    private static final Set<ResolutionTypes> resolutionTypes = Sets.immutableEnumSet(
            ResolutionTypes.DAILY,
            ResolutionTypes.WEEKLY,
            ResolutionTypes.MONTHLY,
            ResolutionTypes.MONTHLY_ADJUSTED,
            ResolutionTypes.YEARLY);


    @Before
    public void setup() {
        userData = new UserData();
        User testUser = new User();
        testUser.setId("TEST_ID");
        UserProfile userProfile = new UserProfile();
        userProfile.setLocale("sv_SE");
        testUser.setProfile(userProfile);
        userData.setUser(testUser);
        accountBalanceHistory = Lists.newArrayList();
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.JANUARY, 1);
        testDate = cal.getTime();

        // SAVINGS
        savingAccounts = Lists.newArrayList();
        savingAccounts.add(createAccount(AccountTypes.SAVINGS));
        savingAccounts.add(createAccount(AccountTypes.PENSION));

        // CARDS_AND_ACCOUNTS
        cardsAndAccountsAccounts = Lists.newArrayList();
        cardsAndAccountsAccounts.add(createAccount(AccountTypes.CREDIT_CARD));
        cardsAndAccountsAccounts.add(createAccount(AccountTypes.CHECKING));

        // LOANS
        loanAccounts = Lists.newArrayList();
        loanAccounts.add(createAccount(AccountTypes.LOAN));
        loanAccounts.add(createAccount(AccountTypes.MORTGAGE));

        monthlyAdjustedPeriodizationFunction = new MonthlyAdjustedPeriodizationFunction(25);
    }

    @Test
    public void testYearlyResolution1() {

        // AccountHistory(testDate) & AccountHistory(testDate+1).
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(savingAccounts.get(0), testDate, 3000.0));
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(savingAccounts.get(1), DateUtils.addDays(testDate, 1), 1500.0));
        userData.setAccountBalanceHistory(accountBalanceHistory);

        // Compute statistics on user data and filter by year.
        List<Statistic> statistics = filterByResolution(aggregateAccountBalances(savingAccounts),
                ResolutionTypes.YEARLY);

        assertEquals(1, statistics.size());
        assertEquals("2016", statistics.get(0).getPeriod());
        assertEquals(4500.0, statistics.get(0).getValue(), 0.001);
    }

    @Test
    public void testYearlyResolution2() {

        // AccountHistory(testDate) & AccountHistory(previous year).
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(cardsAndAccountsAccounts.get(0), testDate, 100.0));
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(cardsAndAccountsAccounts.get(1), DateUtils.addDays(testDate, -1), 700.0));
        userData.setAccountBalanceHistory(accountBalanceHistory);

        // Compute statistics on user data and filter by year.
        List<Statistic> statistics = filterByResolution(aggregateAccountBalances(cardsAndAccountsAccounts),
                ResolutionTypes.YEARLY);

        // Check size and value.
        assertEquals(2, statistics.size());
        statistics.forEach(s -> {
            switch (s.getPeriod()) {
            case "2016":
                assertEquals(100.0, s.getValue(), 0.001);
                break;
            case "2015":
                assertEquals(700.0, s.getValue(), 0.001);
                break;
            default:
                fail("Unexpected period: " + s.getPeriod());
            }
        });
    }

    @Test
    public void testMonthlyResolution1() {

        // AccountHistory(testDate), AccountHistory(month+1) & AccountHistory(month+2).
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(savingAccounts.get(0), testDate, 900.0));
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(savingAccounts.get(1), DateUtils.addMonths(testDate, 1), -200.0));
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(savingAccounts.get(1), DateUtils.addMonths(testDate, 2), 100.0));
        userData.setAccountBalanceHistory(accountBalanceHistory);

        // Compute statistics on user data and filter by month.
        List<Statistic> statistics = filterByResolution(aggregateAccountBalances(savingAccounts),
                ResolutionTypes.MONTHLY);

        // Check size and values.
        assertEquals(3, statistics.size());
        statistics.forEach(s -> {
            switch (s.getPeriod()) {
            case "2016-01":
                assertEquals(900.0, s.getValue(), 0.001);
                break;
            case "2016-02":
                assertEquals(-200.0, s.getValue(), 0.001);
                break;
            case "2016-03":
                assertEquals(100.0, s.getValue(), 0.001);
                break;
            default:
                fail("Unexpected period: " + s.getPeriod());
            }
        });
    }

    @Test
    public void testMonthlyResolution2() {

        // AccountHistory(testDate), AccountHistory(month+1) & AccountHistory(month+1).
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(savingAccounts.get(0), testDate, 500.0));
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(savingAccounts.get(0), DateUtils.addDays(testDate, 31), 200.0));
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(savingAccounts.get(1), DateUtils.addDays(testDate, 32), 100.0));
        userData.setAccountBalanceHistory(accountBalanceHistory);

        // Compute statistics on user data and filter by month.
        List<Statistic> statistics = filterByResolution(aggregateAccountBalances(savingAccounts),
                ResolutionTypes.MONTHLY);

        // Check size and values.
        assertEquals(2, statistics.size());
        statistics.forEach(s -> {
            switch (s.getPeriod()) {
            case "2016-01":
                assertEquals(500.0, s.getValue(), 0.001);
                break;
            case "2016-02":
                assertEquals(300.0, s.getValue(), 0.001);
                break;
            default:
                fail("Unexpected period: " + s.getPeriod());
            }
        });
    }

    @Test
    public void testMonthlyAdjustedResolution() {

        // AccountHistory(testDate), AccountHistory(day after monthly adjusted day).
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(cardsAndAccountsAccounts.get(0), testDate, 200.0));
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(cardsAndAccountsAccounts.get(0),
                DateUtils.addDays(testDate, 25), 100.0));
        userData.setAccountBalanceHistory(accountBalanceHistory);

        // Compute statistics on user data and filter by monthly adjusted.
        List<Statistic> statistics = filterByResolution(aggregateAccountBalances(cardsAndAccountsAccounts),
                ResolutionTypes.MONTHLY_ADJUSTED);

        // Check size and values.
        assertEquals(2, statistics.size());
        statistics.forEach(s -> {
            switch (s.getPeriod()) {
            case "2016-01":
                assertEquals(200.0, s.getValue(), 0.001);
                break;
            case "2016-02":
                assertEquals(100.0, s.getValue(), 0.001);
                break;
            default:
                fail("Unexpected period: " + s.getPeriod());
            }
        });
    }

    @Test
    public void testWeeklyResolution() {

        // AccountHistory(testDate), AccountHistory(day in next week).
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(cardsAndAccountsAccounts.get(0), testDate, 600.0));
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(cardsAndAccountsAccounts.get(0), DateUtils.addDays(testDate, 3), 200.0));
        userData.setAccountBalanceHistory(accountBalanceHistory);

        // Compute statistics on user data and filter by week.
        List<Statistic> statistics = filterByResolution(aggregateAccountBalances(cardsAndAccountsAccounts),
                ResolutionTypes.WEEKLY);

        // Check size and values.
        assertEquals(2, statistics.size());
        statistics.forEach(s -> {
            switch (s.getPeriod()) {
            case "2015:53":
                assertEquals(600.0, s.getValue(), 0.001);
                break;
            case "2016:01":
                assertEquals(200.0, s.getValue(), 0.001);
                break;
            default:
                fail("Unexpected period: " + s.getPeriod());
            }
        });
    }

    @Test
    public void testDailyResolution1() {

        // AccountHistory(testDate), AccountHistory(testDate+1) & AccountHistory(testDate+2).
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(cardsAndAccountsAccounts.get(0), testDate, 250.0));
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(cardsAndAccountsAccounts.get(0), DateUtils.addDays(testDate, 1), 200.0));
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(cardsAndAccountsAccounts.get(1), DateUtils.addDays(testDate, 2), 100.0));
        userData.setAccountBalanceHistory(accountBalanceHistory);

        // Compute statistics on user data and filter by day.
        List<Statistic> statistics = filterByResolution(aggregateAccountBalances(cardsAndAccountsAccounts),
                ResolutionTypes.DAILY);

        // Check size and values.
        assertEquals(3, statistics.size());
        statistics.forEach(s -> {
            switch (s.getPeriod()) {
            case "2016-01-01":
                assertEquals(250.0, s.getValue(), 0.001);
                break;
            case "2016-01-02":
                assertEquals(200, s.getValue(), 0.001);
                break;
            case "2016-01-03":
                assertEquals(100, s.getValue(), 0.001);
                break;
            default:
                fail("Unexpected period: " + s.getPeriod());
            }
        });
    }

    @Test
    public void testDailyResolution2() {

        // AccountHistory(testDate), AccountHistory(testDate) & AccountHistory(month+1).
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(cardsAndAccountsAccounts.get(0), testDate, 800.0));
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(cardsAndAccountsAccounts.get(1), testDate, 100.0));
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(cardsAndAccountsAccounts.get(0), DateUtils.addMonths(testDate, 1), 400.0));
        userData.setAccountBalanceHistory(accountBalanceHistory);

        // Compute statistics on user data and filter by day.
        List<Statistic> statistics = filterByResolution(aggregateAccountBalances(cardsAndAccountsAccounts),
                ResolutionTypes.DAILY);

        // Check size and values.
        assertEquals(statistics.size(), 32);
        statistics.forEach(s -> {
            if (s.getPeriod().contains("2016-01")) {
                assertEquals(900.0, s.getValue(), 0);
            } else {
                assertEquals(400.0, s.getValue(), 0);
            }
        });
    }

    @Test
    public void testAggregateAccountGroups1() {

        // 2 AccountHistory(SAVINGS), 2 AccountHistory(LOANS) & 2 AccountHistory(CARDS_AND_ACCOUNTS).
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(savingAccounts.get(0), testDate, 200.0));
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(savingAccounts.get(1), testDate, 100.0));
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(loanAccounts.get(0), testDate, 700.0));
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(loanAccounts.get(1), testDate, 400.0));
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(cardsAndAccountsAccounts.get(0), testDate, 600.0));
        accountBalanceHistory.add(createAccountBalanceHistoryEntry(cardsAndAccountsAccounts.get(1), testDate, 200.0));
        userData.setAccountBalanceHistory(accountBalanceHistory);

        // Compute statistics on user data and filter by day.
        List<Account> allAccounts = Lists.newArrayList(
                Iterables.concat(savingAccounts, loanAccounts, cardsAndAccountsAccounts));
        List<Statistic> statistics = filterByResolution(aggregateAccountBalances(allAccounts),
                ResolutionTypes.DAILY);

        // Check total size and that daily values aggregate for every category.
        assertEquals(3, statistics.size());
        statistics.forEach(s -> {
            switch (s.getDescription()) {
            case "cards-and-accounts":
                assertEquals(800.0, s.getValue(), 0.001);
                break;
            case "loans":
                assertEquals(1100, s.getValue(), 0.001);
                break;
            case "savings":
                assertEquals(300.0, s.getValue(), 0.001);
                break;
            default:
                fail("Unexpected description: " + s.getDescription());
            }
        });
    }

    /**
     * Helper method for aggregating account balances by account type for a list of accounts.
     */
    private List<Statistic> aggregateAccountBalances(List<Account> accounts) {
        return resolutionTypes.stream()
                .map(resolutionType -> aggregateAccountBalances(accounts, resolutionType))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<Statistic> aggregateAccountBalances(List<Account> accounts, ResolutionTypes resolutionType){
            return StatisticsGeneratorAggregator.aggregateAccountBalances(
                    userData,
                    Statistic.Types.BALANCES_BY_ACCOUNT_TYPE_GROUP,
                    resolutionType,
                    monthlyAdjustedPeriodizationFunction,
                    new AccountBalanceToAccountTypeFunction(accounts),
                    StatisticsGeneratorFunctions.STATISTICS_GROUP_FUNCTION,
                    StatisticsSumming::reduce);
    }

    /**
     * Helper method for creating a single computed account history.
     */
    private AccountBalance createAccountBalanceHistoryEntry(Account account, Date date, double balance) {
        return AccountBalanceUtils.createEntry(
                account.getUserId(),
                account.getId(),
                date,
                balance,
                date.getTime());
    }

    /**
     * Helper method for creating an account by type.
     */
    private Account createAccount(AccountTypes type) {
        Account account = new Account();
        account.setId(UUIDUtils.toTinkUUID(UUID.randomUUID()));
        account.setUserId(UUIDUtils.toTinkUUID(UUID.randomUUID()));
        account.setType(type);
        return account;
    }

    /**
     * Helper method for filtering a list of statistics based on resolution type.
     */
    private List<Statistic> filterByResolution(List<Statistic> statistics, final ResolutionTypes type) {
        Predicate<Statistic> predicate = statistic -> statistic.getResolution() == type;
        statistics = FluentIterable.from(statistics).filter(predicate).toList();
        return statistics;
    }

}
