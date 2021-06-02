package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.banks.sbab.entities.TransactionEntity;

@Ignore
public class TransactionEntityTest {

    @Test
    public void testAmountIsCorrectWhenRightCurrencyOrNoCurrency() {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setTransactionDate("2016-01-01");

        // Allow both kr and sek (and ignore case).
        transactionEntity.setAmount("150.00 kr");
        Assert.assertEquals(transactionEntity.toTinkTransaction(false).get().getAmount(), 150, 0);
        transactionEntity.setAmount("100.00 sek");
        Assert.assertEquals(transactionEntity.toTinkTransaction(false).get().getAmount(), 100, 0);
        transactionEntity.setAmount("50.00 Sek");
        Assert.assertEquals(transactionEntity.toTinkTransaction(false).get().getAmount(), 50, 0);
        transactionEntity.setAmount("20.00kr");
        Assert.assertEquals(transactionEntity.toTinkTransaction(false).get().getAmount(), 20, 0);
        transactionEntity.setAmount("30.00");
        Assert.assertEquals(transactionEntity.toTinkTransaction(false).get().getAmount(), 30, 0);
    }

    @Test
    public void testNegativeAmounts() {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setTransactionDate("2016-01-01");

        transactionEntity.setAmount("-150.00");
        Assert.assertEquals(transactionEntity.toTinkTransaction(false).get().getAmount(), -150, 0);

        transactionEntity.setAmount("-150");
        Assert.assertEquals(transactionEntity.toTinkTransaction(false).get().getAmount(), -150, 0);

        transactionEntity.setAmount("-1.00");
        Assert.assertEquals(transactionEntity.toTinkTransaction(false).get().getAmount(), -1, 0);
    }

    @Test
    public void nullOrEmptyAmountIsNotOkay() {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setTransactionDate("2016-01-01");

        Assert.assertFalse(transactionEntity.toTinkTransaction(false).isPresent());

        transactionEntity.setAmount("");
        Assert.assertFalse(transactionEntity.toTinkTransaction(false).isPresent());

        transactionEntity.setAmount(" ");
        Assert.assertFalse(transactionEntity.toTinkTransaction(false).isPresent());

        transactionEntity.setAmount("       ");
        Assert.assertFalse(transactionEntity.toTinkTransaction(false).isPresent());
    }

    @Test
    public void nullOrEmptyDateIsNotOkay() {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setAmount("1500.00 kr");

        Assert.assertFalse(transactionEntity.toTinkTransaction(false).isPresent());

        transactionEntity.setTransactionDate("");
        Assert.assertFalse(transactionEntity.toTinkTransaction(false).isPresent());

        transactionEntity.setTransactionDate(" ");
        Assert.assertFalse(transactionEntity.toTinkTransaction(false).isPresent());

        transactionEntity.setTransactionDate("       ");
        Assert.assertFalse(transactionEntity.toTinkTransaction(false).isPresent());
    }

    @Test
    public void nullOrEmptyDescriptionIsOkay() {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setAmount("15.00 kr");
        transactionEntity.setTransactionDate("2016-01-01");

        Assert.assertTrue(transactionEntity.toTinkTransaction(false).isPresent());

        transactionEntity.setDescriptionFrom("");
        Assert.assertTrue(transactionEntity.toTinkTransaction(false).isPresent());

        transactionEntity.setDescriptionFrom(" ");
        Assert.assertTrue(transactionEntity.toTinkTransaction(false).isPresent());
    }

    @Test
    public void wrongFormatOfDateIsNotOkay() throws Exception {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setAmount("1.50 kr");

        transactionEntity.setTransactionDate("This is not a date");
        Assert.assertFalse(transactionEntity.toTinkTransaction(false).isPresent());
    }
}
