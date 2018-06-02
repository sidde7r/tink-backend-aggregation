package se.tink.backend.system.workers.statistics;

import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.common.utils.AccountBalanceUtils;
import se.tink.backend.common.workers.statistics.account.AccountBalanceHistoryCalculator;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountBalance;
import se.tink.backend.core.Transaction;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.StringUtils;

public class AccountBalanceHistoryCalculatorTest {

    private Account account;
    private List<AccountBalance> actualHistory;
    private List<AccountBalance> calculatedHistory;
    private AccountBalanceHistoryCalculator calculator;
    private Date today;
    private List<Transaction> transactions;

    @Before
    public void setup() throws Exception {
        account = new Account();
        account.setUserId(StringUtils.generateUUID());
        actualHistory = Lists.newArrayList();
        calculator = new AccountBalanceHistoryCalculator();
        calculatedHistory = Lists.newArrayList();
        today = new Date();
        transactions = Lists.newArrayList();
    }

    @Test
    public void testReturnsBalance() throws Exception {

        account.setBalance(500.0);

        
        // Calculate with empty account history and empty transactions.
        calculatedHistory = calculator.calculate(account, transactions, actualHistory, false);

        Assert.assertEquals(1, calculatedHistory.size());
        Assert.assertEquals(500.0, calculatedHistory.get(0).getBalance(), 0);

        
        // Calculate with empty account history and one transaction today.
        transactions.add(createTransaction(today, -100.0));
        calculatedHistory = calculator.calculate(account, transactions, actualHistory, false);

        Assert.assertEquals(1, calculatedHistory.size());
        Assert.assertEquals(500.0, calculatedHistory.get(0).getBalance(), 0);
    }

    @Test
    public void testCalculateWhenNoTransactions1() throws Exception {

        account.setBalance(500.0);
        
        // AccountBalance(yesterday).
        actualHistory.add(createActualAccountHistory(DateUtils.addDays(today, -1), 700.0));

        calculatedHistory = calculator.calculate(account, transactions, actualHistory, false);

        Assert.assertEquals(2, calculatedHistory.size());
        
        Assert.assertEquals(700.0, calculatedHistory.get(0).getBalance(), 0);
        Assert.assertEquals(500.0, calculatedHistory.get(1).getBalance(), 0);
    }

    @Test
    public void testCalculateWhenNoTransactions2() throws Exception {

        // AccountBalance(yesterday), AccountBalance(today).
        actualHistory.add(createActualAccountHistory(DateUtils.addDays(today, -1), 700.0));
        actualHistory.add(createActualAccountHistory(today, 100.0));

        calculatedHistory = calculator.calculate(account, transactions, actualHistory, false);

        Assert.assertEquals(2, calculatedHistory.size());
        
        Assert.assertEquals(700.0, calculatedHistory.get(0).getBalance(), 0); // today-1
        Assert.assertEquals(100.0, calculatedHistory.get(1).getBalance(), 0); // today
    }

    @Test
    public void testCalculateWhenNoTransactions3() throws Exception {

        // AccountHistory(four days ago), AccountHistory(yesterday), AccountHistory(today).
        actualHistory.add(createActualAccountHistory(DateUtils.addDays(today, -4), 900.0));
        actualHistory.add(createActualAccountHistory(DateUtils.addDays(today, -1), 700.0));
        actualHistory.add(createActualAccountHistory(today, 100.0));

        calculatedHistory = calculator.calculate(account, transactions, actualHistory, false);

        Assert.assertEquals(5, calculatedHistory.size());
        
        Assert.assertEquals(900.0, calculatedHistory.get(0).getBalance(), 0); // today-4
        Assert.assertEquals(900.0, calculatedHistory.get(1).getBalance(), 0); // today-3
        Assert.assertEquals(900.0, calculatedHistory.get(2).getBalance(), 0); // today-2
        Assert.assertEquals(700.0, calculatedHistory.get(3).getBalance(), 0); // today-1
        Assert.assertEquals(100.0, calculatedHistory.get(4).getBalance(), 0); // today
    }

    @Test
    public void testCalculateWhenHistoryAndTransactions1() throws Exception {

        // Transaction(yesterday), AccountBalance(today).
        transactions.add(createTransaction(DateUtils.addDays(today, -1), -200.0));
        actualHistory.add(createActualAccountHistory(today, 1000.0));

        calculatedHistory = calculator.calculate(account, transactions, actualHistory, false);

        Assert.assertEquals(2, calculatedHistory.size());
        
        Assert.assertEquals(1000.0, calculatedHistory.get(0).getBalance(), 0); // today-1
        Assert.assertEquals(1000.0, calculatedHistory.get(1).getBalance(), 0); // today
    }

    @Test
    public void testCalculateWhenHistoryAndTransactions2() throws Exception {

        // Transaction(2 days ago), AccountBalance(today).
        transactions.add(createTransaction(DateUtils.addDays(today, -2), -300.0));
        actualHistory.add(createActualAccountHistory(today, 1000.0));

        calculatedHistory = calculator.calculate(account, transactions, actualHistory, false);

        Assert.assertEquals(3, calculatedHistory.size());
        
        Assert.assertEquals(1000.0, calculatedHistory.get(0).getBalance(), 0); // today-2
        Assert.assertEquals(1000.0, calculatedHistory.get(1).getBalance(), 0); // today-1
        Assert.assertEquals(1000.0, calculatedHistory.get(2).getBalance(), 0); // today
    }

    @Test
    public void testCalculateWhenHistoryAndTransactions3() throws Exception {

        // AccountBalance(yesterday), AccountBalance(today), Transaction(today).
        actualHistory.add(createActualAccountHistory(DateUtils.addDays(today, -1), 5000.0));
        actualHistory.add(createActualAccountHistory(today, 1000.0));
        transactions.add(createTransaction(today, 700.0));

        calculatedHistory = calculator.calculate(account, transactions, actualHistory, false);

        Assert.assertEquals(2, calculatedHistory.size());
        
        Assert.assertEquals(5000.0, calculatedHistory.get(0).getBalance(), 0); // today-1
        Assert.assertEquals(1000.0, calculatedHistory.get(1).getBalance(), 0); // today
    }

    @Test
    public void testCalculateWhenHistoryAndTransactions4() throws Exception {

        // Transaction(2 days ago), Transaction(yesterday), AccountBalance(today).
        transactions.add(createTransaction(DateUtils.addDays(today, -2), 50.0));
        transactions.add(createTransaction(DateUtils.addDays(today, -1), -700.0));
        actualHistory.add(createActualAccountHistory(today, 2000.0));

        calculatedHistory = calculator.calculate(account, transactions, actualHistory, false);

        Assert.assertEquals(3, calculatedHistory.size());
        
        Assert.assertEquals(2700.0, calculatedHistory.get(0).getBalance(), 0); // today-2
        Assert.assertEquals(2000.0, calculatedHistory.get(1).getBalance(), 0); // today-1
        Assert.assertEquals(2000.0, calculatedHistory.get(2).getBalance(), 0); // today
    }

    @Test
    public void testCalculateWhenHistoryAndTransactions5() throws Exception {

        // Transaction(4 days ago), Transaction(yesterday), AccountBalance(today).
        transactions.add(createTransaction(DateUtils.addDays(today, -4), -500.0));
        transactions.add(createTransaction(DateUtils.addDays(today, -1), -100.0));
        actualHistory.add(createActualAccountHistory(today, 4000.0));

        calculatedHistory = calculator.calculate(account, transactions, actualHistory, false);

        Assert.assertEquals(5, calculatedHistory.size());
        
        Assert.assertEquals(4100.0, calculatedHistory.get(0).getBalance(), 0); // today-4
        Assert.assertEquals(4100.0, calculatedHistory.get(1).getBalance(), 0); // today-3
        Assert.assertEquals(4100.0, calculatedHistory.get(2).getBalance(), 0); // today-2
        Assert.assertEquals(4000.0, calculatedHistory.get(3).getBalance(), 0); // today-1
        Assert.assertEquals(4000.0, calculatedHistory.get(4).getBalance(), 0); // today
    }

    @Test
    public void testCalculateWhenHistoryAndTransactions6() throws Exception {

        // AccountBalance(3 days ago), Transaction(yesterday), AccountBalance(today).
        actualHistory.add(createActualAccountHistory(DateUtils.addDays(today, -3), 2500.0));
        transactions.add(createTransaction(DateUtils.addDays(today, -1), -200.0));
        actualHistory.add(createActualAccountHistory(today, 1000.0));

        calculatedHistory = calculator.calculate(account, transactions, actualHistory, false);

        Assert.assertEquals(4, calculatedHistory.size());
        
        Assert.assertEquals(2500.0, calculatedHistory.get(0).getBalance(), 0); // today-3
        Assert.assertEquals(2500.0, calculatedHistory.get(1).getBalance(), 0); // today-2
        Assert.assertEquals(2300.0, calculatedHistory.get(2).getBalance(), 0); // today-1
        Assert.assertEquals(1000.0, calculatedHistory.get(3).getBalance(), 0); // today
    }

    @Test
    public void testDontPadHistoryIfDisabledAccount1() throws Exception {

        // AccountBalance(today-3), AccountBalance(today-2).
        actualHistory.add(createActualAccountHistory(DateUtils.addDays(today, -3), 500.0));
        actualHistory.add(createActualAccountHistory(DateUtils.addDays(today, -2), 1000.0));

        calculatedHistory = calculator.calculate(account, transactions, actualHistory, true);

        Assert.assertEquals(4, calculatedHistory.size());
        
        // Check that the balance is 0 after latest account history.
        Assert.assertEquals(500.0, calculatedHistory.get(0).getBalance(), 0); // today-3
        Assert.assertEquals(1000.0, calculatedHistory.get(1).getBalance(), 0); // today-2
        Assert.assertEquals(0.0, calculatedHistory.get(2).getBalance(), 0); // today-1
        Assert.assertEquals(0.0, calculatedHistory.get(3).getBalance(), 0); // today
    }

    @Test
    public void testDontPadHistoryIfDisabledAccount2() throws Exception {

        // AccountBalance(today-4), Transaction(today-3), Transaction(today-2)
        transactions.add(createTransaction(DateUtils.addDays(today, -4), -100.0));
        transactions.add(createTransaction(DateUtils.addDays(today, -3), -200.0));
        actualHistory.add(createActualAccountHistory(DateUtils.addDays(today, -2), 1000.0));

        calculatedHistory = calculator.calculate(account, transactions, actualHistory, true);

        Assert.assertEquals(5, calculatedHistory.size());
        
        // Check that the balance is 0 after latest account history.
        Assert.assertEquals(1200.0, calculatedHistory.get(0).getBalance(), 0); // today-4
        Assert.assertEquals(1000.0, calculatedHistory.get(1).getBalance(), 0); // today-3
        Assert.assertEquals(1000.0, calculatedHistory.get(2).getBalance(), 0); // today-2
        Assert.assertEquals(0.0, calculatedHistory.get(3).getBalance(), 0); // today-1
        Assert.assertEquals(0.0, calculatedHistory.get(4).getBalance(), 0); // today
    }

    /**
     * Helper method for creating a single transaction.
     */
    private Transaction createTransaction(Date date, double amount) {
        Transaction transaction = new Transaction();
        transaction.setOriginalDate(date);
        transaction.setAmount(amount);
        return transaction;
    }

    /**
     * Helper method for creating a single account history.
     */
    private AccountBalance createActualAccountHistory(Date date, double balance) {
        return AccountBalanceUtils.createEntry(account.getUserId(), account.getId(), date, balance, date.getTime());
    }
}
