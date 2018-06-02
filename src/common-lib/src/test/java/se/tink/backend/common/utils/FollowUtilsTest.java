package se.tink.backend.common.utils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StringDoublePair;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.follow.FollowData;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.main.TestUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.StringUtils;

public abstract class FollowUtilsTest {
    protected final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    protected List<Category> categories;
    protected List<Account> accounts;
    protected User user;
    protected SECategories categoryConfiguration;

    @Before
    public void setUp() {
        user = getUser();
        categories = generateCategories();
        accounts = generateAccounts();
        categoryConfiguration = new SECategories();
    }

    // Creates transactions for `period`, starts from the 10 day of month, one transaction per day.
    // If (`numberOfTransactions` + 10) is bigger than number of days in the month, than will be thrown exception
    protected List<Transaction> createTransactions(String period, int categoryIndex, int numberOfTransactions)
            throws ParseException {
        List<Transaction> transactions = Lists.newArrayList();
        int offset = 10;

        for (int i = 0; i < numberOfTransactions; i++) {
            transactions.add(createTransaction(StringUtils.generateUUID(), period + "-" + (offset + i), categoryIndex,
                    false));
        }

        return transactions;
    }

    protected Statistic createStatistic(String description, String period, double amount, String type) {
        return TestUtils
                .createStatistic(description, period, amount, user.getProfile().getPeriodMode(), type, user.getId(),
                        null);
    }

    protected User getUser() {
        User user = TestUtils.createUser("Test");
        user.getProfile().setPeriodMode(ResolutionTypes.MONTHLY);

        return user;
    }

    protected List<Category> generateCategories() {
        SECategories categoryConfiguration = new SECategories();

        List<Category> categories = Lists.newArrayList(
                TestUtils.createCategory(SECategories.Codes.INCOME_SALARY_OTHER), //0
                TestUtils.createCategory(SECategories.Codes.EXPENSES_FOOD_GROCERIES), //1
                TestUtils.createCategory(SECategories.Codes.EXPENSES_MISC_UNCATEGORIZED), //2
                TestUtils.createCategory(SECategories.Codes.TRANSFERS_EXCLUDE_OTHER), // 3
                TestUtils.createCategory(SECategories.Codes.TRANSFERS_SAVINGS_OTHER), // 4
                TestUtils.createCategory(SECategories.Codes.EXPENSES_FOOD_COFFEE), // 5
                TestUtils.createCategory(SECategories.Codes.EXPENSES_FOOD)); // 6 // parent category

        setParent(categories.get(5), categories.get(6));

        return categories;
    }

    protected void setParent(Category category, Category parentCategory) {
        category.setParent(parentCategory.getId());
        parentCategory.setSecondaryName(null);
    }

    protected List<Account> generateAccounts() {
        return Lists.newArrayList(TestUtils.createAccount(1000, false),
                TestUtils.createAccount(100, true));
    }

    protected Transaction createTransaction(String description, String date, int categoryIndex, boolean excludedAccount)
            throws ParseException {
        int amount = categoryIndex == 0 ? 20 : -20;
        String accountId = excludedAccount ? accounts.get(1).getId() : accounts.get(0).getId();

        return TestUtils.createTransaction(description, amount, formatter.parse(date), user.getId(),
                categories.get(categoryIndex), accountId);
    }

    protected FollowData constructTransactionalFollowItemData(String period, String currentPeriod,
            String currentPeriodEndDate, List<Transaction> transactions, boolean includeTransactions)
            throws ParseException {
        return FollowUtils
                .constructTransactionalFollowItemData(period, currentPeriod,
                        DateUtils.inclusiveEndTime(formatter.parse(currentPeriodEndDate)),
                        includeTransactions, user, transactions, accounts, categories, categoryConfiguration);
    }

    protected void populateExpensesFollowItem(FollowItem followItem, String period, String periodEndDate,
            List<Transaction> transactions, boolean includeTransactions, boolean suggest)
            throws ParseException {
        FollowUtils.populateExpensesFollowItem(followItem, period, period,
                DateUtils.inclusiveEndTime(formatter.parse(periodEndDate)),
                includeTransactions, suggest, user, transactions, accounts, categories, categoryConfiguration);
    }

    protected void populateSearchFollowItem(FollowItem followItem, String period, String periodEndDate,
            List<Transaction> transactions, boolean includeTransactions, boolean suggest)
            throws ParseException {
        FollowUtils.populateSearchFollowItem(followItem, period, period,
                DateUtils.inclusiveEndTime(formatter.parse(periodEndDate)),
                includeTransactions, suggest, user, transactions, accounts, categories, categoryConfiguration);
    }

    protected void populateSavingsFollowItem(FollowItem followItem, String period, String periodEndDate,
            List<Transaction> transactions, List<Statistic> statistics, boolean includeTransactions, boolean suggest,
            boolean includeHistoricalAmount)
            throws ParseException {
        FollowUtils.populateSavingsFollowItem(followItem, period, period,
                DateUtils.inclusiveEndTime(formatter.parse(periodEndDate)), includeHistoricalAmount,
                includeTransactions, suggest, user, transactions, statistics, categories);
    }

    protected void assertFollowData(FollowData followData, String expPeriod, int expHistoricalAmountSize,
            int expPeriodAmountSize, double expPeriodProgress, boolean includePeriodTransaction) {
        Assert.assertEquals("Incorrect follow period", expPeriod, followData.getPeriod());
        Assert.assertEquals("Incorrect size of historical amount", expHistoricalAmountSize,
                followData.getHistoricalAmounts().size());
        Assert.assertEquals("Incorrect size of period amount", expPeriodAmountSize,
                followData.getPeriodAmounts().size());
        Assert.assertEquals("Incorrect period progress", expPeriodProgress, followData.getPeriodProgress(), 0.001);

        if (includePeriodTransaction) {
            Assert.assertNotNull("Period transaction was not found", followData.getPeriodTransactions());
        } else {
            Assert.assertNull("Unexpected period transaction", followData.getPeriodTransactions());
        }
    }

    protected void assertAmount(StringDoublePair amount, String expPeriod, double expValue) {
        Assert.assertEquals("Incorrect period name", expPeriod, amount.getKey());
        Assert.assertEquals("Incorrect amount value", expValue, amount.getValue(), 0.001);
    }

    protected void assertFollowItem(FollowItem followItem, double expFollowProgress, String expPeriod,
            String expPeriodEndDate) {
        Assert.assertNotNull("Follow data was not found", followItem.getData());
        Assert.assertEquals("Incorrect follow progress", expFollowProgress, followItem.getProgress(), 0.001);
        Assert.assertEquals("Incorrect last period in periodAmounts", expPeriodEndDate,
                Iterables.getLast(followItem.getData().getPeriodAmounts()).getKey());
        Assert.assertEquals("Incorrect last period in historicalAmounts", expPeriod,
                Iterables.getLast(followItem.getData().getHistoricalAmounts()).getKey());

        boolean expPositiveProgress = false;
        if (expFollowProgress < 1) {
            expPositiveProgress = true;
        }

        Assert.assertEquals(expPositiveProgress, followItem.isProgressPositive());
    }

}
