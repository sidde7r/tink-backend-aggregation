package se.tink.backend.common.utils;

import com.google.common.collect.Lists;
import java.text.ParseException;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.follow.FollowData;

public class ConstructTransactionalFollowItemDataTest extends FollowUtilsTest {

    @Test
    public void testExpectAllTransactions() throws ParseException {
        String period = "2016-05";
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("ICA ", "2016-05-08", 1, false));
        transactions.add(createTransaction("Hemhop", "2016-05-18", 1, false));
        transactions.add(createTransaction("ICA", "2016-06-03", 1, false));

        FollowData followData = constructTransactionalFollowItemData(period, currentPeriod, currentPeriodEndDate,
                transactions, false);

        int expHistoricalAmountSize = 2;
        int expPeriodAmountSize = 31;
        double expPeriodProgress = 1.;

        assertFollowData(followData, period, expHistoricalAmountSize, expPeriodAmountSize, expPeriodProgress, false);

        // assert HistoricalAmounts
        assertAmount(followData.getHistoricalAmounts().get(0), "2016-05", -40);
        assertAmount(followData.getHistoricalAmounts().get(1), "2016-06", -20);

        // assert PeriodAmounts
        assertAmount(followData.getPeriodAmounts().get(6), "2016-05-07", 0);
        assertAmount(followData.getPeriodAmounts().get(7), "2016-05-08", -20);
        assertAmount(followData.getPeriodAmounts().get(30), "2016-05-31", -40);
    }

    @Test
    public void testSkipTransactionsAfterCurrentPeriod() throws ParseException {
        String period = "2016-05";
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("ICA ", "2016-05-08", 1, false));
        transactions.add(createTransaction("Hemhop", "2016-05-18", 1, false));
        transactions.add(createTransaction("ICA", "2016-06-03", 1, false));
        transactions.add(createTransaction("Hemhop", "2016-06-28", 1, false));

        FollowData followData = constructTransactionalFollowItemData(period, currentPeriod, currentPeriodEndDate,
                transactions, false);

        int expHistoricalAmountSize = 2;
        int expPeriodAmountSize = 31;
        double expPeriodProgress = 1.;

        assertFollowData(followData, period, expHistoricalAmountSize, expPeriodAmountSize, expPeriodProgress, false);

        // assert HistoricalAmounts
        assertAmount(followData.getHistoricalAmounts().get(0), "2016-05", -40);
        assertAmount(followData.getHistoricalAmounts().get(1), "2016-06", -20);

        // assert PeriodAmounts
        assertAmount(followData.getPeriodAmounts().get(6), "2016-05-07", 0);
        assertAmount(followData.getPeriodAmounts().get(7), "2016-05-08", -20);
        assertAmount(followData.getPeriodAmounts().get(30), "2016-05-31", -40);
    }

    @Test
    public void testSkipTransactionsForExcludedCategory() throws ParseException {
        String period = "2016-05";
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("ICA ", "2016-05-08", 1, false));
        transactions.add(createTransaction("Hemhop", "2016-05-18", 1, false));
        transactions.add(createTransaction("Hemhop", "2016-05-28", 3, false));
        transactions.add(createTransaction("ICA", "2016-06-03", 1, false));

        FollowData followData = constructTransactionalFollowItemData(period, currentPeriod, currentPeriodEndDate,
                transactions, false);

        int expHistoricalAmountSize = 2;
        int expPeriodAmountSize = 31;
        double expPeriodProgress = 1.;

        assertFollowData(followData, period, expHistoricalAmountSize, expPeriodAmountSize, expPeriodProgress, false);

        // assert HistoricalAmounts
        assertAmount(followData.getHistoricalAmounts().get(0), "2016-05", -40);
        assertAmount(followData.getHistoricalAmounts().get(1), "2016-06", -20);

        // assert PeriodAmounts
        assertAmount(followData.getPeriodAmounts().get(6), "2016-05-07", 0);
        assertAmount(followData.getPeriodAmounts().get(7), "2016-05-08", -20);
        assertAmount(followData.getPeriodAmounts().get(30), "2016-05-31", -40);
    }

    @Test
    public void testSkipTransactionsForExcludedAccount() throws ParseException {
        String period = "2016-05";
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("ICA ", "2016-05-08", 1, false));
        transactions.add(createTransaction("Hemhop", "2016-05-18", 1, false));
        transactions.add(createTransaction("Hemhop", "2016-05-28", 1, true));
        transactions.add(createTransaction("ICA", "2016-06-03", 1, false));

        FollowData followData = constructTransactionalFollowItemData(period, currentPeriod, currentPeriodEndDate,
                transactions, false);

        int expHistoricalAmountSize = 2;
        int expPeriodAmountSize = 31;
        double expPeriodProgress = 1.;

        assertFollowData(followData, period, expHistoricalAmountSize, expPeriodAmountSize, expPeriodProgress, false);

        // assert HistoricalAmounts
        assertAmount(followData.getHistoricalAmounts().get(0), "2016-05", -40);
        assertAmount(followData.getHistoricalAmounts().get(1), "2016-06", -20);

        // assert PeriodAmounts
        assertAmount(followData.getPeriodAmounts().get(6), "2016-05-07", 0);
        assertAmount(followData.getPeriodAmounts().get(7), "2016-05-08", -20);
        assertAmount(followData.getPeriodAmounts().get(30), "2016-05-31", -40);
    }

    @Test
    public void testNoSkipForNoExcludedCategory() throws ParseException {
        String period = "2016-05";
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("ICA ", "2016-05-08", 4, false));
        transactions.add(createTransaction("Hemhop", "2016-05-18", 1, false));
        transactions.add(createTransaction("Hemhop", "2016-05-28", 2, false));
        transactions.add(createTransaction("ICA", "2016-06-03", 0, false));

        FollowData followData = constructTransactionalFollowItemData(period, currentPeriod, currentPeriodEndDate,
                transactions, false);

        int expHistoricalAmountSize = 2;
        int expPeriodAmountSize = 31;
        double expPeriodProgress = 1.;

        assertFollowData(followData, period, expHistoricalAmountSize, expPeriodAmountSize, expPeriodProgress, false);

        // assert HistoricalAmounts
        assertAmount(followData.getHistoricalAmounts().get(0), "2016-05", -60);
        assertAmount(followData.getHistoricalAmounts().get(1), "2016-06", 20);

        // assert PeriodAmounts
        assertAmount(followData.getPeriodAmounts().get(6), "2016-05-07", 0);
        assertAmount(followData.getPeriodAmounts().get(7), "2016-05-08", -20);
        assertAmount(followData.getPeriodAmounts().get(25), "2016-05-26", -40);
        assertAmount(followData.getPeriodAmounts().get(30), "2016-05-31", -60);
    }

    @Test
    public void testLimitHistoricalAmountsForLast12Months() throws ParseException {
        String period = "2016-05";
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.addAll(createTransactions("2015-05", 1, 1));
        transactions.addAll(createTransactions("2015-06", 1, 2));
        transactions.addAll(createTransactions("2015-07", 1, 3));
        transactions.addAll(createTransactions("2015-08", 1, 4));
        transactions.addAll(createTransactions("2015-09", 1, 5));
        transactions.addAll(createTransactions("2015-10", 1, 6));
        transactions.addAll(createTransactions("2015-11", 1, 7));
        transactions.addAll(createTransactions("2015-12", 1, 8));
        transactions.addAll(createTransactions("2016-01", 1, 9));
        transactions.addAll(createTransactions("2016-02", 1, 10));
        transactions.addAll(createTransactions("2016-03", 1, 11));
        transactions.addAll(createTransactions("2016-04", 1, 12));
        transactions.addAll(createTransactions("2016-05", 1, 13));
        transactions.addAll(createTransactions("2016-06", 1, 14));

        FollowData followData = constructTransactionalFollowItemData(period, currentPeriod, currentPeriodEndDate,
                transactions, false);

        int expHistoricalAmountSize = 12;
        int expPeriodAmountSize = 31;
        double expPeriodProgress = 1.;

        assertFollowData(followData, period, expHistoricalAmountSize, expPeriodAmountSize, expPeriodProgress, false);

        // assert HistoricalAmounts
        assertAmount(followData.getHistoricalAmounts().get(0), "2015-07", 3 * -20);
        assertAmount(followData.getHistoricalAmounts().get(5), "2015-12", 8 * -20);
        assertAmount(followData.getHistoricalAmounts().get(11), "2016-06", 14 * -20);

        // assert PeriodAmounts
        assertAmount(followData.getPeriodAmounts().get(8), "2016-05-09", 0);
        assertAmount(followData.getPeriodAmounts().get(9), "2016-05-10", -20);
        assertAmount(followData.getPeriodAmounts().get(14), "2016-05-15", 6 * -20);
        assertAmount(followData.getPeriodAmounts().get(30), "2016-05-31", 13 * -20);
    }

    @Test
    public void testLimitHistoricalAmountsForLast12MonthsWithGap() throws ParseException {
        String period = "2016-05";
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.addAll(createTransactions("2015-05", 1, 1));
        transactions.addAll(createTransactions("2015-06", 1, 2));
        transactions.addAll(createTransactions("2015-07", 1, 3));
        transactions.addAll(createTransactions("2015-12", 1, 8));
        transactions.addAll(createTransactions("2016-01", 1, 9));
        transactions.addAll(createTransactions("2016-02", 1, 10));
        transactions.addAll(createTransactions("2016-03", 1, 11));
        transactions.addAll(createTransactions("2016-05", 1, 13));

        FollowData followData = constructTransactionalFollowItemData(period, currentPeriod, currentPeriodEndDate,
                transactions, false);

        int expHistoricalAmountSize = 12;
        int expPeriodAmountSize = 31;
        double expPeriodProgress = 1.;

        assertFollowData(followData, period, expHistoricalAmountSize, expPeriodAmountSize, expPeriodProgress, false);

        // assert HistoricalAmounts
        assertAmount(followData.getHistoricalAmounts().get(0), "2015-07", 3 * -20);
        assertAmount(followData.getHistoricalAmounts().get(4), "2015-11", 0);
        assertAmount(followData.getHistoricalAmounts().get(7), "2016-02", 10 * -20);
        assertAmount(followData.getHistoricalAmounts().get(11), "2016-06", 0);

        // assert PeriodAmounts
        assertAmount(followData.getPeriodAmounts().get(8), "2016-05-09", 0);
        assertAmount(followData.getPeriodAmounts().get(9), "2016-05-10", -20);
        assertAmount(followData.getPeriodAmounts().get(14), "2016-05-15", 6 * -20);
        assertAmount(followData.getPeriodAmounts().get(30), "2016-05-31", 13 * -20);
    }

    @Test
    public void testEmptyPeriodAmountsForNoTransactionsByPeriod() throws ParseException {
        String period = "2016-05";
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.addAll(createTransactions("2015-05", 1, 1));
        transactions.addAll(createTransactions("2015-06", 1, 2));
        transactions.addAll(createTransactions("2015-07", 1, 3));
        transactions.addAll(createTransactions("2015-12", 1, 8));
        transactions.addAll(createTransactions("2016-01", 1, 9));
        transactions.addAll(createTransactions("2016-02", 1, 10));
        transactions.addAll(createTransactions("2016-03", 1, 11));
        transactions.addAll(createTransactions("2016-06", 1, 13));

        FollowData followData = constructTransactionalFollowItemData(period, currentPeriod, currentPeriodEndDate,
                transactions, false);

        int expHistoricalAmountSize = 12;
        int expPeriodAmountSize = 31;
        double expPeriodProgress = 1.;

        assertFollowData(followData, period, expHistoricalAmountSize, expPeriodAmountSize, expPeriodProgress, false);

        // assert HistoricalAmounts
        assertAmount(followData.getHistoricalAmounts().get(0), "2015-07", 3 * -20);
        assertAmount(followData.getHistoricalAmounts().get(4), "2015-11", 0);
        assertAmount(followData.getHistoricalAmounts().get(7), "2016-02", 10 * -20);
        assertAmount(followData.getHistoricalAmounts().get(11), "2016-06", 13 * -20);

        // assert PeriodAmounts
        assertAmount(followData.getPeriodAmounts().get(8), "2016-05-09", 0);
        assertAmount(followData.getPeriodAmounts().get(9), "2016-05-10", 0);
        assertAmount(followData.getPeriodAmounts().get(14), "2016-05-15", 0);
        assertAmount(followData.getPeriodAmounts().get(30), "2016-05-31", 0);
    }

    @Test
    public void testExpectPeriodAmountsWhenPeriodIsOneYearAgoFromCurrentPeriod() throws ParseException {
        String period = "2015-06";
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("ICA ", "2015-06-08", 1, false));
        transactions.add(createTransaction("Hemhop", "2015-07-18", 1, false));
        transactions.add(createTransaction("ICA", "2016-04-18", 1, false));
        transactions.add(createTransaction("Hemhop", "2016-06-18", 1, false));

        FollowData followData = constructTransactionalFollowItemData(period, currentPeriod, currentPeriodEndDate,
                transactions, false);

        int expHistoricalAmountSize = 12;
        int expPeriodAmountSize = 30;
        double expPeriodProgress = 1.;

        assertFollowData(followData, period, expHistoricalAmountSize, expPeriodAmountSize, expPeriodProgress, false);

        // assert HistoricalAmounts
        assertAmount(followData.getHistoricalAmounts().get(0), "2015-07", -20);
        assertAmount(followData.getHistoricalAmounts().get(10), "2016-05", 0);
        assertAmount(followData.getHistoricalAmounts().get(11), "2016-06", -20);

        // assert PeriodAmounts
        assertAmount(followData.getPeriodAmounts().get(6), "2015-06-07", 0);
        assertAmount(followData.getPeriodAmounts().get(7), "2015-06-08", -20);
        assertAmount(followData.getPeriodAmounts().get(29), "2015-06-30", -20);
    }

    @Test
    public void testExpectPeriodAmountsWhenPeriodIsCurrentPeriod() throws ParseException {
        String period = "2016-06";
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.addAll(createTransactions("2016-03", 1, 11));
        transactions.addAll(createTransactions("2016-06", 1, 19));

        FollowData followData = constructTransactionalFollowItemData(period, currentPeriod, currentPeriodEndDate,
                transactions, false);

        int expHistoricalAmountSize = 4;
        int expPeriodAmountSize = 25;
        double expPeriodProgress = 25. / 30;

        assertFollowData(followData, period, expHistoricalAmountSize, expPeriodAmountSize, expPeriodProgress, false);

        // assert HistoricalAmounts
        assertAmount(followData.getHistoricalAmounts().get(0), "2016-03", 11 * -20);
        assertAmount(followData.getHistoricalAmounts().get(1), "2016-04", 0);
        assertAmount(followData.getHistoricalAmounts().get(3), "2016-06", 16 * -20); // by 2016-06-25

        // assert PeriodAmounts
        assertAmount(followData.getPeriodAmounts().get(8), "2016-06-09", 0);
        assertAmount(followData.getPeriodAmounts().get(9), "2016-06-10", -20);
        assertAmount(followData.getPeriodAmounts().get(14), "2016-06-15", 6 * -20);
        assertAmount(followData.getPeriodAmounts().get(24), "2016-06-25", 16 * -20);
    }

    @Test
    public void testExpectPeriodAmountsWhenPeriodIsCurrentPeriodForMonthAdjusted() throws ParseException {
        user.getProfile().setPeriodMode(ResolutionTypes.MONTHLY_ADJUSTED);
        user.getProfile().setPeriodAdjustedDay(25);

        String period = "2016-06";
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-15";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.addAll(createTransactions("2016-03", 1, 11));
        transactions.addAll(createTransactions("2016-05", 1, 20));
        transactions.addAll(createTransactions("2016-06", 1, 19));

        FollowData followData = constructTransactionalFollowItemData(period, currentPeriod, currentPeriodEndDate,
                transactions, false);

        int expHistoricalAmountSize = 4;
        int expPeriodAmountSize = 22;
        double expPeriodProgress = 22. / 29;

        assertFollowData(followData, period, expHistoricalAmountSize, expPeriodAmountSize, expPeriodProgress, false);

        // assert HistoricalAmounts
        assertAmount(followData.getHistoricalAmounts().get(0), "2016-03", 11 * -20);
        assertAmount(followData.getHistoricalAmounts().get(1), "2016-04", 0);
        assertAmount(followData.getHistoricalAmounts().get(3), "2016-06",
                (5 + 6) * -20); // 6 transactions for May and 6 for June

        // assert PeriodAmounts
        assertAmount(followData.getPeriodAmounts().get(0), "2016-05-25", -20);
        assertAmount(followData.getPeriodAmounts().get(4), "2016-05-29", 5 * -20);
        assertAmount(followData.getPeriodAmounts().get(5), "2016-05-30", 5 * -20);
        assertAmount(followData.getPeriodAmounts().get(16), "2016-06-10", 6 * -20);
        assertAmount(followData.getPeriodAmounts().get(21), "2016-06-15", 11 * -20);
    }

    @Test
    public void testSumTransactionsForSameDate() throws ParseException {
        String period = "2016-05";
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("ICA ", "2016-05-08", 1, false));
        transactions.add(createTransaction("Hemhop", "2016-05-18", 1, false));
        transactions.add(createTransaction("ICA", "2016-05-18", 1, false));
        transactions.add(createTransaction("Hemhop", "2016-05-18", 1, false));

        FollowData followData = constructTransactionalFollowItemData(period, currentPeriod, currentPeriodEndDate,
                transactions, false);

        int expHistoricalAmountSize = 2;
        int expPeriodAmountSize = 31;
        double expPeriodProgress = 1.;

        assertFollowData(followData, period, expHistoricalAmountSize, expPeriodAmountSize, expPeriodProgress, false);

        // assert HistoricalAmounts
        assertAmount(followData.getHistoricalAmounts().get(0), "2016-05", 4 * -20);
        assertAmount(followData.getHistoricalAmounts().get(1), "2016-06", 0);

        // assert PeriodAmounts
        assertAmount(followData.getPeriodAmounts().get(0), "2016-05-01", 0);
        assertAmount(followData.getPeriodAmounts().get(7), "2016-05-08", -20);
        assertAmount(followData.getPeriodAmounts().get(16), "2016-05-17", -20);
        assertAmount(followData.getPeriodAmounts().get(17), "2016-05-18", -80);
        assertAmount(followData.getPeriodAmounts().get(30), "2016-05-31", -80);
    }

    @Test
    public void testIncludePeriodTransactions() throws ParseException {
        String period = "2016-04";
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("ICA ", "2016-05-08", 1, false));
        transactions.add(createTransaction("Hemhop", "2016-05-18", 1, false));
        transactions.add(createTransaction("ICA", "2016-04-18", 1, false));
        transactions.add(createTransaction("Hemhop", "2016-06-18", 1, false));

        FollowData followData = constructTransactionalFollowItemData(period, currentPeriod, currentPeriodEndDate,
                transactions, true);

        int expHistoricalAmountSize = 3;
        int expPeriodAmountSize = 30;
        double expPeriodProgress = 1.;
        int expPeriodTransactionSize = 1;
        Transaction expTransaction = transactions.get(2);

        assertFollowData(followData, period, expHistoricalAmountSize, expPeriodAmountSize, expPeriodProgress, true);

        Assert.assertEquals(expPeriodTransactionSize, followData.getPeriodTransactions().size());
        Assert.assertEquals(expTransaction, followData.getPeriodTransactions().get(0));
    }

    @Test
    public void testIncludeEmptyPeriodTransactionsForNoTransactionsByPeriod() throws ParseException {
        String period = "2016-03";
        String currentPeriod = "2016-06";
        String currentPeriodEndDate = "2016-06-25";

        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(createTransaction("ICA ", "2016-05-08", 1, false));
        transactions.add(createTransaction("Hemhop", "2016-05-18", 1, false));
        transactions.add(createTransaction("ICA", "2016-04-18", 1, false));
        transactions.add(createTransaction("Hemhop", "2016-06-18", 1, false));

        FollowData followData = constructTransactionalFollowItemData(period, currentPeriod, currentPeriodEndDate,
                transactions, true);

        int expHistoricalAmountSize = 3;
        int expPeriodAmountSize = 31;
        double expPeriodProgress = 1.;
        int expPeriodTransactionSize = 0;

        assertFollowData(followData, period, expHistoricalAmountSize, expPeriodAmountSize, expPeriodProgress, true);

        Assert.assertEquals(expPeriodTransactionSize, followData.getPeriodTransactions().size());
    }

}
