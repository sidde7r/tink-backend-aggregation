package se.tink.backend.common.utils;

import com.google.common.collect.Lists;
import java.text.ParseException;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StringDoublePair;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.follow.ExpensesFollowCriteria;
import se.tink.backend.core.follow.FollowCriteria;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.follow.FollowTypes;
import se.tink.backend.core.follow.SavingsFollowCriteria;
import se.tink.backend.core.follow.SearchFollowCriteria;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class PopulateFollowItemTest extends FollowUtilsTest {

    @Test
    public void testEmptyExpensesFollowItemForNoTransactionsByCriteriaCategory() throws ParseException {
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("Test", "2016-06-02", 1, false));
        transactions.add(createTransaction("Test", "2016-06-05", 0, false));
        transactions.add(createTransaction("Test", "2016-06-07", 1, false));
        transactions.add(createTransaction("Test", "2016-06-10", 1, false));

        FollowItem followItem = getFollowItem(getExpensesFollowCriteria(6), FollowTypes.EXPENSES);

        populateExpensesFollowItem(followItem, currentPeriod, currentPeriodEndDate, transactions, false, false);

        double expFollowProgress = 0.;

        assertFollowItem(followItem, expFollowProgress, currentPeriod, currentPeriodEndDate);
    }

    @Test
    public void testFilterTransactionsByCriteriaCategory() throws ParseException {
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("Test", "2016-06-02", 1, false));
        transactions.add(createTransaction("Test", "2016-06-05", 0, false));
        transactions.add(createTransaction("Test", "2016-06-07", 5, false));
        transactions.add(createTransaction("Test", "2016-06-10", 1, false));

        FollowItem followItem = getFollowItem(getExpensesFollowCriteria(5), FollowTypes.EXPENSES);

        double expFollowProgress = -20 / followItem.getFollowCriteria().getTargetAmount();

        populateExpensesFollowItem(followItem, currentPeriod, currentPeriodEndDate, transactions, false, false);

        assertFollowItem(followItem, expFollowProgress, currentPeriod, currentPeriodEndDate);
    }

    @Test
    public void testFilterTransactionsByCriteriaParentCategory() throws ParseException {
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("Test", "2016-06-02", 1, false));
        transactions.add(createTransaction("Test", "2016-06-05", 0, false));
        transactions.add(createTransaction("Test", "2016-06-07", 5, false));
        transactions.add(createTransaction("Test", "2016-06-10", 5, false));

        FollowItem followItem = getFollowItem(getExpensesFollowCriteria(6), FollowTypes.EXPENSES);

        double expFollowProgress = -40 / followItem.getFollowCriteria().getTargetAmount();

        populateExpensesFollowItem(followItem, currentPeriod, currentPeriodEndDate, transactions, false, false);

        assertFollowItem(followItem, expFollowProgress, currentPeriod, currentPeriodEndDate);
    }

    @Test
    public void testFinishedFollowProgress() throws ParseException {
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("Test", "2016-06-02", 1, false));
        transactions.add(createTransaction("Test", "2016-06-05", 0, false));
        transactions.addAll(createTransactions(currentPeriod, 5, 10));

        FollowItem followItem = getFollowItem(getExpensesFollowCriteria(6), FollowTypes.EXPENSES);
        double expFollowProgress = -20 * 10 / followItem.getFollowCriteria().getTargetAmount(); // 2.0

        populateExpensesFollowItem(followItem, currentPeriod, currentPeriodEndDate, transactions, false, false);

        assertFollowItem(followItem, expFollowProgress, currentPeriod, currentPeriodEndDate);
    }

    @Test
    public void testSuggestBasedOnPreviousMonth() throws ParseException {
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("Test", "2016-05-02", 5, false));
        transactions.add(createTransaction("Test", "2016-05-05", 5, false));
        transactions.add(createTransaction("Test", "2016-06-07", 5, false));
        transactions.add(createTransaction("Test", "2016-06-10", 5, false));

        FollowItem followItem = getFollowItem(getExpensesFollowCriteria(6), FollowTypes.EXPENSES);
        double expFollowProgress = 1.;
        double expTargetAmount = -40;

        populateExpensesFollowItem(followItem, currentPeriod, currentPeriodEndDate, transactions, false, true);

        Assert.assertEquals("Incorrect suggested target amount", expTargetAmount,
                followItem.getFollowCriteria().getTargetAmount(), 0.001);
        assertFollowItem(followItem, expFollowProgress, currentPeriod, currentPeriodEndDate);
    }

    @Test
    public void testSuggestBasedOnAvgAmount() throws ParseException {
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("Test", "2016-03-02", 5, false));
        transactions.add(createTransaction("Test", "2016-05-02", 5, false));
        transactions.add(createTransaction("Test", "2016-05-05", 5, false));
        transactions.add(createTransaction("Test", "2016-06-07", 5, false));
        transactions.add(createTransaction("Test", "2016-06-10", 5, false));

        FollowItem followItem = getFollowItem(getExpensesFollowCriteria(6), FollowTypes.EXPENSES);
        double expTargetAmount = -25;
        double expFollowProgress = -40 / expTargetAmount;

        populateExpensesFollowItem(followItem, currentPeriod, currentPeriodEndDate, transactions, false, true);

        Assert.assertEquals("Incorrect suggested target amount", expTargetAmount,
                followItem.getFollowCriteria().getTargetAmount(), 0.001);
        assertFollowItem(followItem, expFollowProgress, currentPeriod, currentPeriodEndDate);
    }

    @Test
    public void testSearchFollowItems() throws ParseException {
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("Test", "2016-02-02", 1, false));
        transactions.add(createTransaction("Test", "2016-04-05", 0, false));
        transactions.add(createTransaction("Test", "2016-06-07", 5, false));
        transactions.add(createTransaction("Test", "2016-06-10", 5, false));

        FollowItem followItem = getFollowItem(getSearchFollowCriteris("Query"), FollowTypes.SEARCH);

        populateSearchFollowItem(followItem, currentPeriod, currentPeriodEndDate, transactions, false, false);

        int expHistoricalAmountSize = 5;
        int expPeriodAmountSize = 25;
        double expPeriodProgress = expPeriodAmountSize / 30.;

        assertFollowData(followItem.getData(), currentPeriod, expHistoricalAmountSize, expPeriodAmountSize,
                expPeriodProgress, false);
        assertAmount(followItem.getData().getHistoricalAmounts().get(2), "2016-04", 20);
        assertAmount(followItem.getData().getHistoricalAmounts().get(4), "2016-06", -40);

        assertAmount(followItem.getData().getPeriodAmounts().get(24), "2016-06-25", -40);
    }

    @Test
    public void testSearchFollowItemsAndSuggest() throws ParseException {
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("Test", "2016-02-02", 1, false));
        transactions.add(createTransaction("Test", "2016-04-05", 0, false));
        transactions.add(createTransaction("Test", "2016-06-07", 5, false));
        transactions.add(createTransaction("Test", "2016-06-10", 5, false));

        FollowItem followItem = getFollowItem(getSearchFollowCriteris("Query string"), FollowTypes.SEARCH);

        populateSearchFollowItem(followItem, currentPeriod, currentPeriodEndDate, transactions, false, true);

        int expHistoricalAmountSize = 5;
        int expPeriodAmountSize = 25;
        double expPeriodProgress = expPeriodAmountSize / 30.;
        double expTargetAmount = -8;
        double expFollowProgress = -40 / expTargetAmount;

        assertFollowItem(followItem, expFollowProgress, currentPeriod, currentPeriodEndDate);
        assertFollowData(followItem.getData(), currentPeriod, expHistoricalAmountSize, expPeriodAmountSize,
                expPeriodProgress, false);
        assertAmount(followItem.getData().getHistoricalAmounts().get(2), "2016-04", 20);
        assertAmount(followItem.getData().getHistoricalAmounts().get(4), "2016-06", -40);

        assertAmount(followItem.getData().getPeriodAmounts().get(24), "2016-06-25", -40);
    }

    @Test
    public void testSavingFollowItemAmountsNullForAllFlagIsFalse() throws ParseException {
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("Test", "2016-02-02", 1, false));
        transactions.add(createTransaction("Test", "2016-04-05", 0, false));
        transactions.add(createTransaction("Test", "2016-06-07", 5, false));
        transactions.add(createTransaction("Test", "2016-06-10", 5, false));

        List<Statistic> statistics = Lists.newArrayList();
        statistics
                .add(createStatistic(accounts.get(0).getId(), currentPeriod, 100, Statistic.Types.BALANCES_BY_ACCOUNT));

        FollowItem followItem = getFollowItem(getSavingFollowCriteria(currentPeriod, 0), FollowTypes.SAVINGS);

        populateSavingsFollowItem(followItem, currentPeriod, currentPeriodEndDate, transactions, statistics, false,
                false, false);

        Assert.assertNotNull(followItem.getData());
        Assert.assertNull(followItem.getData().getHistoricalAmounts());
        Assert.assertNull(followItem.getData().getPeriodAmounts());
        Assert.assertNull(followItem.getData().getPeriodTransactions());
    }

    @Test
    public void testSavingFollowItemWithEmptyHistoricalAmountForWrongStatisticType() throws ParseException {
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("Test", "2016-02-02", 1, false));
        transactions.add(createTransaction("Test", "2016-04-05", 0, false));
        transactions.add(createTransaction("Test", "2016-06-07", 5, false));
        transactions.add(createTransaction("Test", "2016-06-10", 5, false));

        List<Statistic> statistics = Lists.newArrayList();
        statistics.add(createStatistic(accounts.get(0).getId(), currentPeriod,
                100, Statistic.Types.BALANCES_BY_ACCOUNT_TYPE_GROUP));
        statistics.add(createStatistic(accounts.get(1).getId(), currentPeriod,
                100, Statistic.Types.BALANCES_BY_ACCOUNT_TYPE_GROUP));

        FollowItem followItem = getFollowItem(getSavingFollowCriteria(currentPeriod, 0, 1), FollowTypes.SAVINGS);

        populateSavingsFollowItem(followItem, currentPeriod, currentPeriodEndDate, transactions, statistics, false,
                false, true);

        int expHistoricalAmountSize = 1; // current period

        Assert.assertNotNull(followItem.getData());
        Assert.assertNull(followItem.getData().getPeriodAmounts());
        Assert.assertNull(followItem.getData().getPeriodTransactions());
        Assert.assertNotNull(followItem.getData().getHistoricalAmounts());
        Assert.assertEquals(expHistoricalAmountSize, followItem.getData().getHistoricalAmounts().size());
    }

    @Test
    public void testSavingFollowItemWithEmptyHistoricalAmountForNoCriteriaAccountIds() throws ParseException {
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("Test", "2016-02-02", 1, false));
        transactions.add(createTransaction("Test", "2016-04-05", 0, false));
        transactions.add(createTransaction("Test", "2016-06-07", 5, false));
        transactions.add(createTransaction("Test", "2016-06-10", 5, false));

        List<Statistic> statistics = Lists.newArrayList();
        statistics.add(createStatistic(accounts.get(0).getId(), "2016-06", 100, Statistic.Types.BALANCES_BY_ACCOUNT));
        statistics.add(createStatistic(accounts.get(0).getId(), "2016-05", 100, Statistic.Types.BALANCES_BY_ACCOUNT));

        FollowItem followItem = getFollowItem(getSavingFollowCriteria(currentPeriod, 1), FollowTypes.SAVINGS);

        populateSavingsFollowItem(followItem, currentPeriod, currentPeriodEndDate, transactions, statistics, false,
                false, true);

        int expHistoricalAmountSize = 1; // current period

        Assert.assertNotNull(followItem.getData());
        Assert.assertNull(followItem.getData().getPeriodAmounts());
        Assert.assertNull(followItem.getData().getPeriodTransactions());
        Assert.assertNotNull(followItem.getData().getHistoricalAmounts());
        Assert.assertEquals(expHistoricalAmountSize, followItem.getData().getHistoricalAmounts().size());
    }

    @Test
    public void testSavingFollowItemWithEmptyHistoricalAmountForWrongResolution() throws ParseException {
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("Test", "2016-02-02", 1, false));
        transactions.add(createTransaction("Test", "2016-04-05", 0, false));
        transactions.add(createTransaction("Test", "2016-06-07", 5, false));
        transactions.add(createTransaction("Test", "2016-06-10", 5, false));

        List<Statistic> statistics = Lists.newArrayList();
        statistics
                .add(createStatistic(accounts.get(0).getId(), "2016-06-21", 100, Statistic.Types.BALANCES_BY_ACCOUNT));
        statistics.get(0).setResolution(ResolutionTypes.DAILY);

        FollowItem followItem = getFollowItem(getSavingFollowCriteria(currentPeriod, 0), FollowTypes.SAVINGS);

        populateSavingsFollowItem(followItem, currentPeriod, currentPeriodEndDate, transactions, statistics, false,
                false, true);

        int expHistoricalAmountSize = 1; // current period

        Assert.assertNotNull(followItem.getData());
        Assert.assertNull(followItem.getData().getPeriodAmounts());
        Assert.assertNull(followItem.getData().getPeriodTransactions());
        Assert.assertNotNull(followItem.getData().getHistoricalAmounts());
        Assert.assertEquals(expHistoricalAmountSize, followItem.getData().getHistoricalAmounts().size());
    }

    @Test
    public void testSavingFollowItemWithEmptyHistoricalAmountForPeriodAfterCurrentPeriod() throws ParseException {
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("Test", "2016-02-02", 1, false));
        transactions.add(createTransaction("Test", "2016-04-05", 0, false));
        transactions.add(createTransaction("Test", "2016-06-07", 5, false));
        transactions.add(createTransaction("Test", "2016-06-10", 5, false));

        List<Statistic> statistics = Lists.newArrayList();
        statistics.add(createStatistic(accounts.get(0).getId(), "2016-07", 100, Statistic.Types.BALANCES_BY_ACCOUNT));
        statistics.add(createStatistic(accounts.get(0).getId(), "2016-08", 100, Statistic.Types.BALANCES_BY_ACCOUNT));

        FollowItem followItem = getFollowItem(getSavingFollowCriteria(currentPeriod, 0), FollowTypes.SAVINGS);

        populateSavingsFollowItem(followItem, currentPeriod, currentPeriodEndDate, transactions, statistics, false,
                false, true);

        int expHistoricalAmountSize = 1; // current period

        Assert.assertNotNull(followItem.getData());
        Assert.assertNull(followItem.getData().getPeriodAmounts());
        Assert.assertNull(followItem.getData().getPeriodTransactions());
        Assert.assertNotNull(followItem.getData().getHistoricalAmounts());
        Assert.assertEquals(expHistoricalAmountSize, followItem.getData().getHistoricalAmounts().size());
    }

    @Test
    public void testSavingFollowItemWithFilteredHistoricalAmountForAllCriterias() throws ParseException {
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("Test", "2016-02-02", 1, false));
        transactions.add(createTransaction("Test", "2016-04-05", 0, false));
        transactions.add(createTransaction("Test", "2016-06-07", 5, false));
        transactions.add(createTransaction("Test", "2016-06-10", 5, false));

        List<Statistic> statistics = Lists.newArrayList();
        statistics.add(createStatistic(accounts.get(0).getId(), "2015-10", 100, Statistic.Types.BALANCES_BY_ACCOUNT));
        statistics.add(createStatistic(accounts.get(0).getId(), "2016-01",
                100, Statistic.Types.BALANCES_BY_ACCOUNT_TYPE_GROUP)); // statistic type
        statistics.add(createStatistic(accounts.get(1).getId(), "2016-03",
                100, Statistic.Types.BALANCES_BY_ACCOUNT)); // account id
        statistics.add(createStatistic(accounts.get(0).getId(), "2016-05-06",
                100, Statistic.Types.BALANCES_BY_ACCOUNT)); // ResolutionTypes
        statistics.add(createStatistic(accounts.get(0).getId(), "2016-08",
                100, Statistic.Types.BALANCES_BY_ACCOUNT)); // future period
        statistics.get(3).setResolution(ResolutionTypes.DAILY);

        FollowItem followItem = getFollowItem(getSavingFollowCriteria(currentPeriod, 0), FollowTypes.SAVINGS);

        populateSavingsFollowItem(followItem, currentPeriod, currentPeriodEndDate, transactions, statistics, false,
                false, true);

        int expHistoricalAmountSize = 9;
        String expPeriod = "2015-10";
        int expValue = 100;

        Assert.assertNotNull(followItem.getData());
        Assert.assertNull(followItem.getData().getPeriodAmounts());
        Assert.assertNull(followItem.getData().getPeriodTransactions());
        Assert.assertNotNull(followItem.getData().getHistoricalAmounts());
        Assert.assertEquals(expHistoricalAmountSize, followItem.getData().getHistoricalAmounts().size());
        assertAmount(followItem.getData().getHistoricalAmounts().get(0), expPeriod, expValue);
        assertAmount(followItem.getData().getHistoricalAmounts().get(8), currentPeriod, 0);

        for (int i = 1; i < expHistoricalAmountSize; i++) {
            StringDoublePair pair = followItem.getData().getHistoricalAmounts().get(i);
            Assert.assertEquals("Unexpected amount for period " + pair.getKey(), 0, pair.getValue(), 0.001);
        }
    }

    @Test
    public void testSavingFollowItemLimitForLast12Statistics() throws ParseException {
        String currentPeriod = "2016-12";
        String currentPeriodEndDate = "2016-12-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("Test", "2016-02-02", 1, false));
        transactions.add(createTransaction("Test", "2016-04-05", 0, false));
        transactions.add(createTransaction("Test", "2016-06-07", 5, false));
        transactions.add(createTransaction("Test", "2016-06-10", 5, false));

        List<Statistic> statistics = Lists.newArrayList();
        statistics.add(createStatistic(accounts.get(0).getId(), "2015-07", 100, Statistic.Types.BALANCES_BY_ACCOUNT));
        statistics.add(createStatistic(accounts.get(1).getId(), "2016-01", 100, Statistic.Types.BALANCES_BY_ACCOUNT));
        statistics.add(createStatistic(accounts.get(1).getId(), "2016-08", 100, Statistic.Types.BALANCES_BY_ACCOUNT));
        statistics.add(createStatistic(accounts.get(0).getId(), "2016-20", 100, Statistic.Types.BALANCES_BY_ACCOUNT));

        FollowItem followItem = getFollowItem(getSavingFollowCriteria(currentPeriod, 0, 1), FollowTypes.SAVINGS);

        populateSavingsFollowItem(followItem, currentPeriod, currentPeriodEndDate, transactions, statistics, false,
                false, true);

        int expHistoricalAmountSize = 12;

        Assert.assertNotNull(followItem.getData());
        Assert.assertNull(followItem.getData().getPeriodAmounts());
        Assert.assertNull(followItem.getData().getPeriodTransactions());
        Assert.assertNotNull(followItem.getData().getHistoricalAmounts());
        Assert.assertEquals(expHistoricalAmountSize, followItem.getData().getHistoricalAmounts().size());
        assertAmount(followItem.getData().getHistoricalAmounts().get(0), "2016-01", 100.);
        assertAmount(followItem.getData().getHistoricalAmounts().get(6), "2016-07", 0.);
        assertAmount(followItem.getData().getHistoricalAmounts().get(7), "2016-08", 100.);
        assertAmount(followItem.getData().getHistoricalAmounts().get(11), "2016-12", 0.);
    }

    @Test
    public void testSavingFollowItemWithEmptyTransactionsForNoCriteriaAccountIds() throws ParseException {
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("Test", "2016-02-02", 1, false));
        transactions.add(createTransaction("Test", "2016-04-05", 0, false));
        transactions.add(createTransaction("Test", "2016-06-07", 5, false));
        transactions.add(createTransaction("Test", "2016-06-10", 5, false));

        List<Statistic> statistics = Lists.newArrayList();
        statistics.add(createStatistic(accounts.get(0).getId(), "2016-06", 100, Statistic.Types.BALANCES_BY_ACCOUNT));
        statistics.add(createStatistic(accounts.get(0).getId(), "2016-05", 100, Statistic.Types.BALANCES_BY_ACCOUNT));

        FollowItem followItem = getFollowItem(getSavingFollowCriteria(currentPeriod, 1), FollowTypes.SAVINGS);

        populateSavingsFollowItem(followItem, currentPeriod, currentPeriodEndDate, transactions, statistics, true,
                false, false);

        int expTransactionsSize = 0;

        Assert.assertNotNull(followItem.getData());
        Assert.assertNull(followItem.getData().getPeriodAmounts());
        Assert.assertNull(followItem.getData().getHistoricalAmounts());
        Assert.assertNotNull(followItem.getData().getPeriodTransactions());
        Assert.assertEquals(expTransactionsSize, followItem.getData().getPeriodTransactions().size());
    }

    @Test
    public void testSavingFollowItemWithFilteredTransactionsForCriteriaAccountIds() throws ParseException {
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("Test", "2016-02-02", 1, true));
        transactions.add(createTransaction("Test", "2016-04-05", 0, false));
        transactions.add(createTransaction("Test", "2016-06-07", 5, true));
        transactions.add(createTransaction("Test", "2016-06-10", 5, false));

        List<Statistic> statistics = Lists.newArrayList();
        statistics.add(createStatistic(accounts.get(0).getId(), "2016-06", 100, Statistic.Types.BALANCES_BY_ACCOUNT));
        statistics.add(createStatistic(accounts.get(0).getId(), "2016-05", 100, Statistic.Types.BALANCES_BY_ACCOUNT));

        FollowItem followItem = getFollowItem(getSavingFollowCriteria(currentPeriod, 1), FollowTypes.SAVINGS);

        populateSavingsFollowItem(followItem, currentPeriod, currentPeriodEndDate, transactions, statistics, true,
                false, false);

        int expTransactionsSize = 2;
        List<Transaction> expectedPeriodTransactions = Lists.newArrayList(transactions.get(0), transactions.get(2));

        Assert.assertNotNull(followItem.getData());
        Assert.assertNull(followItem.getData().getPeriodAmounts());
        Assert.assertNull(followItem.getData().getHistoricalAmounts());
        Assert.assertNotNull(followItem.getData().getPeriodTransactions());
        Assert.assertEquals(expTransactionsSize, followItem.getData().getPeriodTransactions().size());
        Assert.assertTrue(
                "Expected transactions was not found.\n Expected: " + expectedPeriodTransactions + "\n Found: "
                        + followItem.getData().getPeriodTransactions(),
                followItem.getData().getPeriodTransactions().containsAll(expectedPeriodTransactions));
    }

    @Test
    public void testSavingSuggestBasedOnEmptyHistoricalAmount() throws ParseException {
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("Test", "2016-02-02", 1, true));
        transactions.add(createTransaction("Test", "2016-04-05", 0, false));
        transactions.add(createTransaction("Test", "2016-06-07", 5, true));
        transactions.add(createTransaction("Test", "2016-06-10", 5, false));

        List<Statistic> statistics = Lists.newArrayList();
        statistics.add(createStatistic(accounts.get(0).getId(), "2016-08", 100, Statistic.Types.BALANCES_BY_ACCOUNT));
        statistics.add(createStatistic(accounts.get(0).getId(), "2016-09", 100, Statistic.Types.BALANCES_BY_ACCOUNT));

        FollowItem followItem = getFollowItem(getSavingFollowCriteria(currentPeriod, 0), FollowTypes.SAVINGS);

        populateSavingsFollowItem(followItem, currentPeriod, currentPeriodEndDate, transactions, statistics, false,
                true, true);

        int expHistoricalAmountSize = 1; // current period
        String expTargetPeriod = "2016-08";
        double expTargetAmount = 3000.;

        Assert.assertNotNull(followItem.getData());
        Assert.assertNull(followItem.getData().getPeriodAmounts());
        Assert.assertNull(followItem.getData().getPeriodTransactions());
        Assert.assertNotNull(followItem.getData().getHistoricalAmounts());
        Assert.assertEquals(expHistoricalAmountSize, followItem.getData().getHistoricalAmounts().size());

        SavingsFollowCriteria followCriteria = SerializationUtils
                .deserializeFromString(followItem.getCriteria(), SavingsFollowCriteria.class);
        Assert.assertEquals("Incorrect calculation of criteria target period", expTargetPeriod,
                followCriteria.getTargetPeriod());
        Assert.assertEquals("Incorrect calculation of criteria target amount", expTargetAmount,
                followCriteria.getTargetAmount(), 0.001);
    }

    @Test
    public void testSavingSuggestBasedOnOneMonth() throws ParseException {
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("Test", "2016-02-02", 1, true));
        transactions.add(createTransaction("Test", "2016-04-05", 0, false));
        transactions.add(createTransaction("Test", "2016-06-07", 5, true));
        transactions.add(createTransaction("Test", "2016-06-10", 5, false));

        List<Statistic> statistics = Lists.newArrayList();
        statistics.add(createStatistic(accounts.get(0).getId(), "2016-06", 100, Statistic.Types.BALANCES_BY_ACCOUNT));
        statistics.add(createStatistic(accounts.get(0).getId(), "2016-09", 100, Statistic.Types.BALANCES_BY_ACCOUNT));

        FollowItem followItem = getFollowItem(getSavingFollowCriteria(currentPeriod, 0), FollowTypes.SAVINGS);

        populateSavingsFollowItem(followItem, currentPeriod, currentPeriodEndDate, transactions, statistics, false,
                true, true);

        int expHistoricalAmountSize = 1; // current period
        String expTargetPeriod = "2016-08";
        double expTargetAmount = 3100.;

        Assert.assertNotNull(followItem.getData());
        Assert.assertNull(followItem.getData().getPeriodAmounts());
        Assert.assertNull(followItem.getData().getPeriodTransactions());
        Assert.assertNotNull(followItem.getData().getHistoricalAmounts());
        Assert.assertEquals(expHistoricalAmountSize, followItem.getData().getHistoricalAmounts().size());

        SavingsFollowCriteria followCriteria = SerializationUtils
                .deserializeFromString(followItem.getCriteria(), SavingsFollowCriteria.class);
        Assert.assertEquals("Incorrect calculation of criteria target period", expTargetPeriod,
                followCriteria.getTargetPeriod());
        Assert.assertEquals("Incorrect calculation of criteria target amount", expTargetAmount,
                followCriteria.getTargetAmount(), 0.001);
    }

    @Test
    public void testSavingSuggestBasedOn12Months() throws ParseException {
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("Test", "2016-02-02", 1, true));
        transactions.add(createTransaction("Test", "2016-04-05", 0, false));
        transactions.add(createTransaction("Test", "2016-06-07", 5, true));
        transactions.add(createTransaction("Test", "2016-06-10", 5, false));

        List<Statistic> statistics = Lists.newArrayList();
        statistics.add(createStatistic(accounts.get(0).getId(), "2016-06", 100, Statistic.Types.BALANCES_BY_ACCOUNT));
        statistics.add(createStatistic(accounts.get(0).getId(), "2016-05", 1200, Statistic.Types.BALANCES_BY_ACCOUNT));
        statistics.add(createStatistic(accounts.get(0).getId(), "2016-04", 200, Statistic.Types.BALANCES_BY_ACCOUNT));
        statistics.add(createStatistic(accounts.get(0).getId(), "2016-03", 1100, Statistic.Types.BALANCES_BY_ACCOUNT));
        statistics.add(createStatistic(accounts.get(0).getId(), "2016-02", 300, Statistic.Types.BALANCES_BY_ACCOUNT));
        statistics.add(createStatistic(accounts.get(0).getId(), "2016-01", 1000, Statistic.Types.BALANCES_BY_ACCOUNT));
        statistics.add(createStatistic(accounts.get(0).getId(), "2015-12", 400, Statistic.Types.BALANCES_BY_ACCOUNT));
        statistics.add(createStatistic(accounts.get(0).getId(), "2015-11", 900, Statistic.Types.BALANCES_BY_ACCOUNT));
        statistics.add(createStatistic(accounts.get(0).getId(), "2015-10", 500, Statistic.Types.BALANCES_BY_ACCOUNT));
        statistics.add(createStatistic(accounts.get(0).getId(), "2015-09", 8000, Statistic.Types.BALANCES_BY_ACCOUNT));
        statistics.add(createStatistic(accounts.get(0).getId(), "2015-08", 600, Statistic.Types.BALANCES_BY_ACCOUNT));
        statistics.add(createStatistic(accounts.get(0).getId(), "2015-07", 700, Statistic.Types.BALANCES_BY_ACCOUNT));

        FollowItem followItem = getFollowItem(getSavingFollowCriteria(currentPeriod, 0), FollowTypes.SAVINGS);

        populateSavingsFollowItem(followItem, currentPeriod, currentPeriodEndDate, transactions, statistics, false,
                true, true);

        int expHistoricalAmountSize = 12;
        String expTargetPeriod = "2016-08";
        double expTargetAmount = 4200.;

        Assert.assertNotNull(followItem.getData());
        Assert.assertNull(followItem.getData().getPeriodAmounts());
        Assert.assertNull(followItem.getData().getPeriodTransactions());
        Assert.assertNotNull(followItem.getData().getHistoricalAmounts());
        Assert.assertEquals(expHistoricalAmountSize, followItem.getData().getHistoricalAmounts().size());

        SavingsFollowCriteria followCriteria = SerializationUtils
                .deserializeFromString(followItem.getCriteria(), SavingsFollowCriteria.class);
        Assert.assertEquals("Incorrect calculation of criteria target period", expTargetPeriod,
                followCriteria.getTargetPeriod());
        Assert.assertEquals("Incorrect calculation of criteria target amount", expTargetAmount,
                followCriteria.getTargetAmount(), 0.001);
    }

    @Test
    public void testSavingFollowItemAmountsNullForAllFlagIsTrue() throws ParseException {
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("Test", "2016-02-02", 1, false));
        transactions.add(createTransaction("Test", "2016-04-05", 0, false));
        transactions.add(createTransaction("Test", "2016-06-07", 5, true));
        transactions.add(createTransaction("Test", "2016-06-10", 5, false));

        List<Statistic> statistics = Lists.newArrayList();
        statistics
                .add(createStatistic(accounts.get(0).getId(), currentPeriod, 100, Statistic.Types.BALANCES_BY_ACCOUNT));

        FollowItem followItem = getFollowItem(getSavingFollowCriteria(currentPeriod, 0), FollowTypes.SAVINGS);

        populateSavingsFollowItem(followItem, currentPeriod, currentPeriodEndDate, transactions, statistics, true,
                true, true);

        int expHistoricalAmountSize = 1;
        int expPeriodTransactionSize = 3;
        String expTargetPeriod = "2016-08";
        double expTargetAmount = 3100.;

        Assert.assertNotNull(followItem.getData());
        Assert.assertNotNull(followItem.getData().getHistoricalAmounts());
        Assert.assertNotNull(followItem.getData().getPeriodTransactions());
        Assert.assertEquals(expHistoricalAmountSize, followItem.getData().getHistoricalAmounts().size());
        Assert.assertEquals(expPeriodTransactionSize, followItem.getData().getPeriodTransactions().size());

        SavingsFollowCriteria followCriteria = SerializationUtils
                .deserializeFromString(followItem.getCriteria(), SavingsFollowCriteria.class);
        Assert.assertEquals("Incorrect calculation of criteria target period", expTargetPeriod,
                followCriteria.getTargetPeriod());
        Assert.assertEquals("Incorrect calculation of criteria target amount", expTargetAmount,
                followCriteria.getTargetAmount(), 0.001);
    }

    private FollowItem getFollowItem(FollowCriteria followCriteria, FollowTypes followType) {
        FollowItem followItem = new FollowItem();
        followItem.setCriteria(SerializationUtils.serializeToString(followCriteria));
        followItem.setType(followType);

        return followItem;
    }

    private SavingsFollowCriteria getSavingFollowCriteria(String period, int... accountIndexes) {
        List<String> accountIds = Lists.newArrayList();

        for (int index : accountIndexes) {
            if (index >= accounts.size()) {
                continue;
            }
            accountIds.add(accounts.get(index).getId());
        }

        SavingsFollowCriteria followCriteria = new SavingsFollowCriteria();
        followCriteria.setTargetAmount(-100.);
        followCriteria.setTargetPeriod(period);
        followCriteria.setAccountIds(accountIds);

        return followCriteria;
    }

    private SearchFollowCriteria getSearchFollowCriteris(String query) {
        SearchFollowCriteria followCriteria = new SearchFollowCriteria();
        followCriteria.setQueryString(query);
        followCriteria.setTargetAmount(-100.);

        return followCriteria;
    }

    private ExpensesFollowCriteria getExpensesFollowCriteria(int... categoryIndexes) {
        List<String> categoryIds = Lists.newArrayList();

        for (int index : categoryIndexes) {
            if (index >= categories.size()) {
                continue;
            }
            categoryIds.add(categories.get(index).getId());
        }

        ExpensesFollowCriteria followCriteria = new ExpensesFollowCriteria();
        followCriteria.setTargetAmount(-100.);
        followCriteria.setCategoryIds(categoryIds);

        return followCriteria;
    }

}
