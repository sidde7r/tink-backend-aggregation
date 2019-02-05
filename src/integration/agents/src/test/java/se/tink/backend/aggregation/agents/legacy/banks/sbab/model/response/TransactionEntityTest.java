package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import org.junit.Assert;
import org.junit.Test;

public class TransactionEntityTest {

    @Test
    public void testAmountIsCorrectWhenRightCurrencyOrNoCurrency() {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setDate("2016-01-01");

        // Allow both kr and sek (and ignore case).
        transactionEntity.setAmount("150,00 kr");
        Assert.assertEquals(transactionEntity.toTinkTransaction().get().getAmount(), 150, 0);
        transactionEntity.setAmount("100,00 sek");
        Assert.assertEquals(transactionEntity.toTinkTransaction().get().getAmount(), 100, 0);
        transactionEntity.setAmount("50,00 Sek");
        Assert.assertEquals(transactionEntity.toTinkTransaction().get().getAmount(), 50, 0);
        transactionEntity.setAmount("20,00kr");
        Assert.assertEquals(transactionEntity.toTinkTransaction().get().getAmount(), 20, 0);
        transactionEntity.setAmount("30,00");
        Assert.assertEquals(transactionEntity.toTinkTransaction().get().getAmount(), 30, 0);
    }

    @Test
    public void testNegativeAmounts() {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setDate("2016-01-01");

        transactionEntity.setAmount("-150,00");
        Assert.assertEquals(transactionEntity.toTinkTransaction().get().getAmount(), -150, 0);

        transactionEntity.setAmount("-150");
        Assert.assertEquals(transactionEntity.toTinkTransaction().get().getAmount(), -150, 0);

        transactionEntity.setAmount("-1,00");
        Assert.assertEquals(transactionEntity.toTinkTransaction().get().getAmount(), -1, 0);
    }

    @Test
    public void nullOrEmptyAmountIsNotOkay() {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setDate("2016-01-01");

        Assert.assertFalse(transactionEntity.toTinkTransaction().isPresent());

        transactionEntity.setAmount("");
        Assert.assertFalse(transactionEntity.toTinkTransaction().isPresent());

        transactionEntity.setAmount(" ");
        Assert.assertFalse(transactionEntity.toTinkTransaction().isPresent());

        transactionEntity.setAmount("       ");
        Assert.assertFalse(transactionEntity.toTinkTransaction().isPresent());
    }

    @Test
    public void nullOrEmptyDateIsNotOkay() {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setAmount("150,00 kr");

        Assert.assertFalse(transactionEntity.toTinkTransaction().isPresent());

        transactionEntity.setDate("");
        Assert.assertFalse(transactionEntity.toTinkTransaction().isPresent());

        transactionEntity.setDate(" ");
        Assert.assertFalse(transactionEntity.toTinkTransaction().isPresent());

        transactionEntity.setDate("       ");
        Assert.assertFalse(transactionEntity.toTinkTransaction().isPresent());
    }

    @Test
    public void nullOrEmptyDescriptionIsOkay() {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setAmount("150,00 kr");
        transactionEntity.setDate("2016-01-01");

        Assert.assertTrue(transactionEntity.toTinkTransaction().isPresent());

        transactionEntity.setNote("");
        Assert.assertTrue(transactionEntity.toTinkTransaction().isPresent());

        transactionEntity.setNote(" ");
        Assert.assertTrue(transactionEntity.toTinkTransaction().isPresent());
    }

    @Test
    public void wrongFormatOfDateIsNotOkay() throws Exception {
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setAmount("150,00 kr");

        transactionEntity.setDate("This is not a date");
        Assert.assertFalse(transactionEntity.toTinkTransaction().isPresent());
    }
}
