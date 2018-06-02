package se.tink.backend.system.cronjob.job;

import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import se.tink.backend.core.Account;
import se.tink.backend.core.CassandraTransaction;
import se.tink.backend.core.Transaction;
import se.tink.libraries.uuid.UUIDUtils;
import static org.junit.Assert.assertEquals;

public class BalanceCalculatorTest {

    private static final double EPSILON = 0.01;

    @Test
    public void pendingTransactions_doNotAffectBalance() {
        BalanceCalculator calculator = new BalanceCalculator();

        Account account = new Account();
        List<Account> accounts = Lists.newArrayList(account);

        Transaction nonPending1 = createTransactionForAccount(account, new BigDecimal(-520), false,
                LocalDateTime.of(2017, 9, 25, 15, 20));
        Transaction nonPending2 = createTransactionForAccount(account, new BigDecimal(276.1), false,
                LocalDateTime.of(2017, 10, 2, 9, 1));
        Transaction pending1 = createTransactionForAccount(account, new BigDecimal(-1128.3), true,
                LocalDateTime.of(2017, 10, 2, 13, 37));
        List<Transaction> transactions = Lists.newArrayList(nonPending1, nonPending2, pending1);

        double correctBalance = nonPending1.getOriginalAmount() + nonPending2.getOriginalAmount();
        account.setBalance(correctBalance);

        CalculateBalanceResult result = calculator.calculateBalance(accounts, transactions);

        assertEquals(result.getUsersWithWrongBalance(), 0);
        assertEquals(result.getHighestBalanceDiffGauge(), 0, EPSILON);
        assertEquals(result.getTotalBalanceDiff(), 0, EPSILON);
    }

    @Test
    public void incorrectBalance_isIdentified() {
        BalanceCalculator calculator = new BalanceCalculator();

        Account account = new Account();
        List<Account> accounts = Lists.newArrayList(account);

        Transaction nonPending1 = createTransactionForAccount(account, new BigDecimal(-520), false,
                LocalDateTime.of(2017, 9, 25, 15, 20));
        Transaction nonPending2 = createTransactionForAccount(account, new BigDecimal(276.1), false,
                LocalDateTime.of(2017, 10, 2, 9, 1));
        List<Transaction> transactions = Lists.newArrayList(nonPending1, nonPending2);

        double correctBalance = nonPending1.getOriginalAmount() + nonPending2.getOriginalAmount();
        double incorrectBalance = correctBalance + 1d;
        double diff = Math.abs(correctBalance - incorrectBalance);
        account.setBalance(incorrectBalance);

        CalculateBalanceResult result = calculator.calculateBalance(accounts, transactions);

        assertEquals(1, result.getUsersWithWrongBalance());
        assertEquals(diff, result.getHighestBalanceDiffGauge(), EPSILON);
        assertEquals(diff, result.getTotalBalanceDiff(), EPSILON);
    }

    @Test
    public void balancesOnMultipleAccounts_areCalculatedCorrectly() {
        BalanceCalculator calculator = new BalanceCalculator();

        Account account1 = new Account();
        Account account2 = new Account();
        List<Account> accounts = Lists.newArrayList(account1, account2);

        Transaction nonPending1 = createTransactionForAccount(account1, new BigDecimal(-200), false,
                LocalDateTime.of(2017, 9, 25, 15, 20));
        Transaction nonPending2 = createTransactionForAccount(account2, new BigDecimal(-300), false,
                LocalDateTime.of(2017, 10, 2, 9, 1));
        List<Transaction> transactions = Lists.newArrayList(nonPending1, nonPending2);

        account1.setBalance(nonPending1.getOriginalAmount());
        account2.setBalance(nonPending2.getOriginalAmount());

        CalculateBalanceResult result = calculator.calculateBalance(accounts, transactions);

        assertEquals(0, result.getUsersWithWrongBalance());
        assertEquals(0, result.getHighestBalanceDiffGauge(), EPSILON);
        assertEquals(0, result.getTotalBalanceDiff(), EPSILON);
    }

   @Test
   public void originalAmountIsChosen_notAmountWhichCanBeModifiedByUser() {
       BalanceCalculator calculator = new BalanceCalculator();

       Account account1 = new Account();
       List<Account> accounts = Lists.newArrayList(account1);

       Transaction nonPending1 = createTransactionForAccount(account1, new BigDecimal(-200), false,
               LocalDateTime.of(2017, 9, 25, 15, 20));
       List<Transaction> transactions = Lists.newArrayList(nonPending1);

       // Set the amount to another value. The originalAmount is what the calculator should care about.
       nonPending1.setAmount(-300d);

       account1.setBalance(nonPending1.getOriginalAmount());

       CalculateBalanceResult result = calculator.calculateBalance(accounts, transactions);

       assertEquals(0, result.getUsersWithWrongBalance());
       assertEquals(0, result.getHighestBalanceDiffGauge(), EPSILON);
       assertEquals(0, result.getTotalBalanceDiff(), EPSILON);
   }

    private Transaction createTransactionForAccount(Account account, BigDecimal amount, boolean pending,
            LocalDateTime date) {
        Transaction transaction = new Transaction();

        transaction.setAccountId(account.getId());
        transaction.setOriginalAmount(amount.doubleValue());
        transaction.setPending(pending);
        transaction.setDate(Date.from(date.atZone(ZoneId.systemDefault()).toInstant()));

        return transaction;
    }
}
