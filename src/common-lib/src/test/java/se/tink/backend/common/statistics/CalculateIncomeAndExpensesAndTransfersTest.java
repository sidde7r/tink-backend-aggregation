package se.tink.backend.common.statistics;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Credentials;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;

public class CalculateIncomeAndExpensesAndTransfersTest {

    private List<Account> accounts;
    private List<Credentials> credentials;
    private List<Transaction> similarTransactions;
    private Date testDate;
    private User testUser;
    private UserProfile userProfile;
    private HashMap<String, Category> testCategories;

    @Before
    public void setup() {
        testUser = new User();
        testUser.setId("TEST_ID");
        userProfile = new UserProfile();
        userProfile.setLocale("sv_SE");
        testUser.setProfile(userProfile);
        accounts = Lists.newArrayList();
        testCategories = createTestCategories();
        credentials = Lists.newArrayList();
        similarTransactions = Lists.newArrayList();
        Calendar cal = Calendar.getInstance();
        cal.set(2016, Calendar.JANUARY, 25);
        testDate = cal.getTime(); // testDate is 2016-01-25
        userProfile.setPeriodAdjustedDay(28); // Monthly adjusted is the 28'th every month
    }

    @Test
    public void testDifferentPeriodsWhenMonthlyAdjustedMode() {

        // Set period mode to MONTHLY_ADJUSTED.
        userProfile.setPeriodMode(ResolutionTypes.MONTHLY_ADJUSTED);

        // 2 expenses before monthly adjusted date.
        similarTransactions.add(createTransaction(testDate, -500,
                testCategories.get(SECategories.Codes.EXPENSES_ENTERTAINMENT_CULTURE)));
        similarTransactions.add(createTransaction(DateUtils.addDays(testDate, 2), -100,
                testCategories.get(SECategories.Codes.EXPENSES_ENTERTAINMENT_HOBBY)));

        // 1 income, 1 expense and 1 excluded on/after monthly adjusted date.
        similarTransactions.add(createTransaction(DateUtils.addDays(testDate, 3), 5000,
                testCategories.get(SECategories.Codes.INCOME_SALARY)));
        similarTransactions.add(createTransaction(DateUtils.addDays(testDate, 3), -2000,
                testCategories.get(SECategories.Codes.EXPENSES_ENTERTAINMENT_VACATION)));
        similarTransactions.add(createTransaction(DateUtils.addDays(testDate, 4), -200,
                testCategories.get(SECategories.Codes.TRANSFERS_EXCLUDE_OTHER)));

        // Compute statistics.
        List<Statistic> statistics = StatisticsGeneratorAggregator
                .calculateIncomeAndExpensesAndTransfers(testUser, credentials, accounts, similarTransactions,
                        new ArrayList<>(testCategories.values()), true, new SECategories());
        List<Statistic> expenses = filterByDescription(statistics, CategoryTypes.EXPENSES);
        List<Statistic> incomes = filterByDescription(statistics, CategoryTypes.INCOME);
        List<Statistic> transfers = filterByDescription(statistics, CategoryTypes.TRANSFERS);

        // Assert that monthly adjusted date is taken into account.
        Assert.assertEquals(statistics.size(), 4);
        Assert.assertEquals(expenses.size(), 2);
        Assert.assertEquals(expenses.get(0).getPeriod(), "2016-01");
        Assert.assertEquals(expenses.get(0).getValue(), -600, 0);
        Assert.assertEquals(expenses.get(1).getPeriod(), "2016-02");
        Assert.assertEquals(expenses.get(1).getValue(), -2000, 0);
        Assert.assertEquals(incomes.size(), 1);
        Assert.assertEquals(incomes.get(0).getPeriod(), "2016-02");
        Assert.assertEquals(incomes.get(0).getValue(), 5000, 0);
        Assert.assertEquals(transfers.size(), 1);
        Assert.assertEquals(transfers.get(0).getPeriod(), "2016-02");
        Assert.assertEquals(transfers.get(0).getValue(), 0, 0);
    }

    @Test
    public void testSamePeriodWhenMonthlyAdjustedMode() {

        // Set period mode to MONTHLY_ADJUSTED.
        userProfile.setPeriodMode(ResolutionTypes.MONTHLY_ADJUSTED);

        // 2 transfers, 1 excluded and 1 income before monthly adjusted date and none after.
        similarTransactions
                .add(createTransaction(testDate, -200, testCategories.get(SECategories.Codes.TRANSFERS_SAVINGS)));
        similarTransactions.add(createTransaction(DateUtils.addDays(testDate, 1), 100,
                testCategories.get(SECategories.Codes.TRANSFERS_OTHER_OTHER)));
        similarTransactions.add(createTransaction(DateUtils.addDays(testDate, 1), 5000,
                testCategories.get(SECategories.Codes.TRANSFERS_EXCLUDE_OTHER)));
        similarTransactions.add(createTransaction(DateUtils.addDays(testDate, 2), 1000,
                testCategories.get(SECategories.Codes.INCOME_SALARY)));

        // Compute statistics.
        List<Statistic> statistics = StatisticsGeneratorAggregator
                .calculateIncomeAndExpensesAndTransfers(testUser, credentials, accounts, similarTransactions,
                        new ArrayList<>(testCategories.values()), true, new SECategories());
        List<Statistic> transfers = filterByDescription(statistics, CategoryTypes.TRANSFERS);
        List<Statistic> incomes = filterByDescription(statistics, CategoryTypes.INCOME);

        // Assert that everything is on the same period.
        Assert.assertEquals(statistics.size(), 2);
        Assert.assertEquals(transfers.size(), 1);
        Assert.assertEquals(transfers.get(0).getPeriod(), "2016-01");
        Assert.assertEquals(transfers.get(0).getValue(), -100, 0);
        Assert.assertEquals(incomes.size(), 1);
        Assert.assertEquals(incomes.get(0).getPeriod(), "2016-01");
        Assert.assertEquals(incomes.get(0).getValue(), 1000, 0);
    }

    @Test
    public void testSamePeriodWhenMonthlyMode() {

        // Set period mode to MONTHLY.
        userProfile.setPeriodMode(ResolutionTypes.MONTHLY);

        // 1 expense before monthly adjusted date.
        similarTransactions.add(createTransaction(testDate, -500,
                testCategories.get(SECategories.Codes.EXPENSES_ENTERTAINMENT_CULTURE)));

        // 1 expense and 1 excluded after monthly adjusted date.
        similarTransactions.add(createTransaction(DateUtils.addDays(testDate, 4), -100,
                testCategories.get(SECategories.Codes.EXPENSES_ENTERTAINMENT_VACATION)));
        similarTransactions.add(createTransaction(DateUtils.addDays(testDate, 5), -2000,
                testCategories.get(SECategories.Codes.TRANSFERS_EXCLUDE_OTHER)));

        // Compute statistics.
        List<Statistic> statistics = StatisticsGeneratorAggregator
                .calculateIncomeAndExpensesAndTransfers(testUser, credentials, accounts, similarTransactions,
                        new ArrayList<>(testCategories.values()), true, new SECategories());

        // Assert that monthly adjusted date is ignored and that everything is on the same period.
        Assert.assertEquals(statistics.size(), 2);
        Assert.assertEquals(statistics.get(0).getPeriod(), "2016-01");
        Assert.assertEquals(statistics.get(0).getValue(), -600, 0);
        Assert.assertEquals(statistics.get(0).getDescription(), CategoryTypes.EXPENSES.name());
        Assert.assertEquals(statistics.get(1).getPeriod(), "2016-01");
        Assert.assertEquals(statistics.get(1).getValue(), 0, 0);
        Assert.assertEquals(statistics.get(1).getDescription(), CategoryTypes.TRANSFERS.name());
    }

    @Test
    public void testDifferentPeriodsWhenMonthlyMode() {

        // Set period mode to MONTHLY.
        userProfile.setPeriodMode(ResolutionTypes.MONTHLY);

        // 1 expense on testDate month.
        similarTransactions.add(createTransaction(testDate, -700,
                testCategories.get(SECategories.Codes.EXPENSES_ENTERTAINMENT_CULTURE)));

        // 1 expense and 1 excluded the month after testDate month.
        similarTransactions.add(createTransaction(DateUtils.addMonths(testDate, 1), -900,
                testCategories.get(SECategories.Codes.EXPENSES_ENTERTAINMENT_HOBBY)));
        similarTransactions.add(createTransaction(DateUtils.addMonths(testDate, 1), -2000,
                testCategories.get(SECategories.Codes.TRANSFERS_EXCLUDE_OTHER)));

        // Compute statistics.
        List<Statistic> statistics = StatisticsGeneratorAggregator
                .calculateIncomeAndExpensesAndTransfers(testUser, credentials, accounts, similarTransactions,
                        new ArrayList<>(testCategories.values()), true, new SECategories());
        List<Statistic> expenses = filterByDescription(statistics, CategoryTypes.EXPENSES);

        // Assert that the expenses are on different periods.
        Assert.assertEquals(statistics.size(), 3);
        Assert.assertEquals(expenses.size(), 2);
        Assert.assertEquals(statistics.get(0).getPeriod(), "2016-01");
        Assert.assertEquals(statistics.get(0).getValue(), -700, 0);
        Assert.assertEquals(statistics.get(1).getPeriod(), "2016-02");
        Assert.assertEquals(statistics.get(1).getValue(), -900, 0);
        Assert.assertEquals(statistics.get(2).getPeriod(), "2016-02");
        Assert.assertEquals(statistics.get(2).getValue(), 0, 0);
    }

    /**
     * Helper method for creating a category.
     */
    private Category createCategory(String code, CategoryTypes type) {
        Category category = new Category();
        category.setCode(code);
        category.setType(type);
        return category;
    }

    /**
     * Helper method for creating a single transaction.
     */
    private Transaction createTransaction(Date date, double amount, Category category) {
        Transaction transaction = new Transaction();
        transaction.setDate(date);
        transaction.setAmount(amount);
        transaction.setCategory(category);
        return transaction;
    }

    /**
     * Helper method for filtering a list of statistics based on description (CategoryTypes).
     */
    private List<Statistic> filterByDescription(List<Statistic> statistics, final CategoryTypes type) {
        Predicate<Statistic> predicate = statistic -> statistic.getDescription() == type.name();
        return FluentIterable.from(statistics).filter(predicate).toList();
    }

    /**
     * Helper method for creating the test categories used in the tests.
     */
    private HashMap<String, Category> createTestCategories() {
        HashMap<String, Category> categories = Maps.newHashMap();
        categories.put(SECategories.Codes.EXPENSES_ENTERTAINMENT_CULTURE,
                createCategory(SECategories.Codes.EXPENSES_ENTERTAINMENT_CULTURE, CategoryTypes.EXPENSES));
        categories.put(SECategories.Codes.EXPENSES_ENTERTAINMENT_HOBBY,
                createCategory(SECategories.Codes.EXPENSES_ENTERTAINMENT_HOBBY, CategoryTypes.EXPENSES));
        categories.put(SECategories.Codes.EXPENSES_ENTERTAINMENT_VACATION,
                createCategory(SECategories.Codes.EXPENSES_ENTERTAINMENT_VACATION, CategoryTypes.EXPENSES));
        categories
                .put(SECategories.Codes.INCOME_SALARY, createCategory(SECategories.Codes.INCOME_SALARY, CategoryTypes.INCOME));
        categories.put(SECategories.Codes.TRANSFERS_EXCLUDE_OTHER,
                createCategory(SECategories.Codes.TRANSFERS_EXCLUDE_OTHER, CategoryTypes.TRANSFERS));
        categories.put(SECategories.Codes.TRANSFERS_SAVINGS,
                createCategory(SECategories.Codes.TRANSFERS_SAVINGS, CategoryTypes.TRANSFERS));
        categories.put(SECategories.Codes.TRANSFERS_OTHER_OTHER,
                createCategory(SECategories.Codes.TRANSFERS_OTHER_OTHER, CategoryTypes.TRANSFERS));
        return categories;
    }

}
