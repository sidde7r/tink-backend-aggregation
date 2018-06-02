package se.tink.backend.system.workers.processor.system;

import com.google.common.collect.Lists;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import se.tink.backend.core.Transaction;
import se.tink.libraries.date.DateUtils;
import static org.assertj.core.api.Assertions.assertThat;

public class CertainDateCalculatorTest {
    
    @Test
    public void testNullCertainDateIfNoTransactions() throws ParseException {
        List<Transaction> transactions = Lists.newArrayList();

        Date certainDate = CertainDateCalculator.calculateCertainDate(transactions);
        
        assertThat(certainDate).isNull();
    }

    @Test
    public void testCorrectCertainDateWhenNoPendingTransactions() throws ParseException {
        Date today = new Date();
        
        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(mockTransaction(today, false));
        transactions.add(mockTransaction(DateUtils.addDays(today, -1), false));
        transactions.add(mockTransaction(DateUtils.addDays(today, -2), false));

        Date certainDate = CertainDateCalculator.calculateCertainDate(transactions);
        
        assertThat(certainDate).isInSameDayAs(DateUtils.addDays(today, -30));
    }
    
    @Test
    public void testCorrectCertainDateWithPendingTransactions() throws ParseException {
        Date today = new Date();
        
        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(mockTransaction(today, true));
        transactions.add(mockTransaction(DateUtils.addDays(today, -1), false));
        transactions.add(mockTransaction(DateUtils.addDays(today, -2), false));

        Date certainDate = CertainDateCalculator.calculateCertainDate(transactions);
        
        assertThat(certainDate).isInSameDayAs(DateUtils.addDays(today, -31));
    }
    
    @Test
    public void testCorrectCertainDateWithPendingTransactionsInFuture() throws ParseException {
        Date today = new Date();
        
        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(mockTransaction(DateUtils.addDays(today, 2), true));
        transactions.add(mockTransaction(DateUtils.addDays(today, -1), false));
        transactions.add(mockTransaction(DateUtils.addDays(today, -2), false));
        transactions.add(mockTransaction(DateUtils.addDays(today, -3), false));

        Date certainDate = CertainDateCalculator.calculateCertainDate(transactions);
        
        assertThat(certainDate).isInSameDayAs(DateUtils.addDays(today, -31));
    }
    
    @Test
    public void testCorrectCertainDateIfPendingBeforeNormalTransactionsInFuture() throws ParseException {
        Date today = new Date();
        
        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(mockTransaction(DateUtils.addDays(today, -1), false));
        transactions.add(mockTransaction(DateUtils.addDays(today, -2), true));
        transactions.add(mockTransaction(DateUtils.addDays(today, -3), false));

        Date certainDate = CertainDateCalculator.calculateCertainDate(transactions);
        
        assertThat(certainDate).isInSameDayAs(DateUtils.addDays(today, -31));
    }
    
    @Test
    public void testCertainDateNotInFutrueIfNonPendingInFuture() throws ParseException {
        Date today = new Date();
        
        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(mockTransaction(DateUtils.addDays(today, 31), false));
        transactions.add(mockTransaction(DateUtils.addDays(today, -1), false));
        transactions.add(mockTransaction(DateUtils.addDays(today, -3), false));

        Date certainDate = CertainDateCalculator.calculateCertainDate(transactions);
        
        assertThat(certainDate).isInSameDayAs(DateUtils.addDays(today, -30));
    }
        
    @Test
    public void testUseCertainDateFrom50TransactionsBackIfLaterThan30DaysBack() throws ParseException {
        Date today = new Date();
        
        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(mockTransaction(DateUtils.addDays(today, -1), false));
        transactions.add(mockTransaction(DateUtils.addDays(today, -2), false));
        
        for (int i = 0; i < 51; i++) {
            transactions.add(mockTransaction(DateUtils.addDays(today, -3), false));
        }

        Date certainDate = CertainDateCalculator.calculateCertainDate(transactions);
        
        assertThat(certainDate).isInSameDayAs(DateUtils.addDays(today, -3));
    }
    
    @Test
    public void testCorrectCertainDateFrom50TransactionsBackIfLaterThan30DaysBackIfPendingBeforeNormal() throws ParseException {
        Date today = new Date();
        
        List<Transaction> transactions = Lists.newArrayList();
        transactions.add(mockTransaction(DateUtils.addDays(today, -1), false));
        transactions.add(mockTransaction(DateUtils.addDays(today, -2), false));
        
        for (int i = 0; i < 51; i++) {
            transactions.add(mockTransaction(DateUtils.addDays(today, -3), false));
        }
        transactions.add(mockTransaction(DateUtils.addDays(today, -4), true));

        Date certainDate = CertainDateCalculator.calculateCertainDate(transactions);
        
        assertThat(certainDate).isInSameDayAs(DateUtils.addDays(today, -5));
    }
    
    private Transaction mockTransaction(Date date, boolean pending) {
        Transaction t = new Transaction();
        t.setDescription("Desc");
        t.setOriginalDate(date);
        t.setDate(date);
        t.setPending(pending);
        return t;
    }
}
