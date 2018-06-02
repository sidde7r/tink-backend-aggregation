package se.tink.backend.webhook.rpc;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.firehose.v1.models.Category;
import se.tink.backend.firehose.v1.models.Transaction;

public class WebhookTransactionTest {
    private Transaction transaction;
    @Before
    public void setUpWebhookTransactionTest() {
        transaction = Transaction.newBuilder()
                .setAccountId("00000000000000000000000000000000")
                .setAmount(50.5)
                .setCategoryId("00000000000000000000000000000001")
                .setCategoryType(Category.Type.TYPE_EXPENSES)
                .setDate(1510317057000L)
                .setDescription("Indian Street Food")
                .setNotes("#bestofshow")
                .setType(Transaction.Type.TYPE_CREDIT_CARD).build();
    }

    @Test
    public void firehoseModel_convertToWebhookTransactionCorrectly() {
        WebhookTransaction webhookTransaction = WebhookTransaction.fromFirehoseTransaction(transaction);

        Assert.assertEquals(transaction.getAccountId(), webhookTransaction.getAccountId());
        Assert.assertEquals(transaction.getAmount(), webhookTransaction.getAmount(),0.01);
        Assert.assertEquals(transaction.getCategoryId(), webhookTransaction.getCategoryId());
        Assert.assertEquals("EXPENSES", webhookTransaction.getCategoryType());
        Assert.assertEquals(transaction.getDate(), webhookTransaction.getDate());
        Assert.assertEquals(transaction.getNotes(), webhookTransaction.getNotes());
        Assert.assertEquals("CREDIT_CARD", webhookTransaction.getType());
    }
}
