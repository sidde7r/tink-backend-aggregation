package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import org.joda.time.DateTime;
import org.junit.Test;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class PaymentEntityTest {
    private static final String ACCOUNT_NUMBER_1 = "01234";
    private static final String ACCOUNT_NUMBER_2 = "56789";

    @Test
    public void getHash_whenFirstFourAmountDecimalsEqual_hashesEquals() {
        RecipientEntity recipientEntity = new RecipientEntity();
        recipientEntity.setGiroNumber(new BankGiroIdentifier("7308596").toUriAsString());

        PaymentEntity paymentEntity1 = new PaymentEntity();
        paymentEntity1.setFromAccount(ACCOUNT_NUMBER_1);
        paymentEntity1.setAmount(1.00001);
        paymentEntity1.setDate(new DateTime().withTimeAtStartOfDay().getMillis());
        paymentEntity1.setReference("37578936060100475");
        paymentEntity1.setRecipient(recipientEntity);

        PaymentEntity paymentEntity2 = new PaymentEntity();
        paymentEntity2.setFromAccount(ACCOUNT_NUMBER_1);
        paymentEntity2.setAmount(1.0);
        paymentEntity2.setDate(new DateTime().withTimeAtStartOfDay().getMillis());
        paymentEntity2.setReference("37578936060100475");
        paymentEntity2.setRecipient(recipientEntity);

        assertEquals(paymentEntity1.calculateHash(), paymentEntity2.calculateHash());
    }

    @Test
    public void getHash_whenAnyOfFirstFourDecimalsDiffer_hashesDiffer() {
        RecipientEntity recipientEntity = new RecipientEntity();
        recipientEntity.setGiroNumber(new BankGiroIdentifier("7308596").toUriAsString());

        PaymentEntity paymentEntity1 = new PaymentEntity();
        paymentEntity1.setAmount(1.0001);
        paymentEntity1.setDate(new DateTime().withTimeAtStartOfDay().getMillis());
        paymentEntity1.setFromAccount(ACCOUNT_NUMBER_1);
        paymentEntity1.setRecipient(recipientEntity);
        paymentEntity1.setReference("37578936060100475");

        PaymentEntity paymentEntity2 = new PaymentEntity();
        paymentEntity2.setAmount(1.0);
        paymentEntity2.setDate(new DateTime().withTimeAtStartOfDay().getMillis());
        paymentEntity2.setFromAccount(ACCOUNT_NUMBER_1);
        paymentEntity2.setRecipient(recipientEntity);
        paymentEntity2.setReference("37578936060100475");

        assertNotEquals(paymentEntity1.calculateHash(), paymentEntity2.calculateHash());
    }

    @Test
    public void getHash_whenGiroNumberDiffer_hashesDiffer() {
        RecipientEntity recipientEntity1 = new RecipientEntity();
        recipientEntity1.setGiroNumber(new BankGiroIdentifier("7308596").toUriAsString());

        PaymentEntity paymentEntity1 = new PaymentEntity();
        paymentEntity1.setAmount(1.0);
        paymentEntity1.setDate(new DateTime().withTimeAtStartOfDay().getMillis());
        paymentEntity1.setFromAccount(ACCOUNT_NUMBER_1);
        paymentEntity1.setRecipient(recipientEntity1);
        paymentEntity1.setReference("37578936060100475");

        RecipientEntity recipientEntity2 = new RecipientEntity();
        recipientEntity2.setGiroNumber(new BankGiroIdentifier("7308595").toUriAsString());

        PaymentEntity paymentEntity2 = new PaymentEntity();
        paymentEntity2.setAmount(1.0);
        paymentEntity2.setDate(new DateTime().withTimeAtStartOfDay().getMillis());
        paymentEntity2.setFromAccount(ACCOUNT_NUMBER_1);
        paymentEntity2.setRecipient(recipientEntity2);
        paymentEntity2.setReference("37578936060100475");

        assertNotEquals(paymentEntity1.calculateHash(), paymentEntity2.calculateHash());
    }

    @Test
    public void getHash_whenReferenceDiffer_hashesDiffer() {
        RecipientEntity recipientEntity = new RecipientEntity();
        recipientEntity.setGiroNumber(new BankGiroIdentifier("7308596").toUriAsString());

        PaymentEntity paymentEntity1 = new PaymentEntity();
        paymentEntity1.setAmount(1.0);
        paymentEntity1.setDate(new DateTime().withTimeAtStartOfDay().getMillis());
        paymentEntity1.setFromAccount(ACCOUNT_NUMBER_1);
        paymentEntity1.setRecipient(recipientEntity);
        paymentEntity1.setReference("37578936060100476");

        PaymentEntity paymentEntity2 = new PaymentEntity();
        paymentEntity2.setAmount(1.0);
        paymentEntity2.setDate(new DateTime().withTimeAtStartOfDay().getMillis());
        paymentEntity2.setFromAccount(ACCOUNT_NUMBER_1);
        paymentEntity2.setRecipient(recipientEntity);
        paymentEntity2.setReference("37578936060100475");

        assertNotEquals(paymentEntity1.calculateHash(), paymentEntity2.calculateHash());
    }

    @Test
    public void getHash_whenAccountNumberDiffer_hashesDiffer() {
        RecipientEntity recipientEntity = new RecipientEntity();
        recipientEntity.setGiroNumber(new BankGiroIdentifier("7308596").toUriAsString());

        PaymentEntity paymentEntity1 = new PaymentEntity();
        paymentEntity1.setAmount(1.0001);
        paymentEntity1.setDate(new DateTime().withTimeAtStartOfDay().getMillis());
        paymentEntity1.setFromAccount(ACCOUNT_NUMBER_2);
        paymentEntity1.setRecipient(recipientEntity);
        paymentEntity1.setReference("37578936060100475");

        PaymentEntity paymentEntity2 = new PaymentEntity();
        paymentEntity2.setAmount(1.0);
        paymentEntity2.setDate(new DateTime().withTimeAtStartOfDay().getMillis());
        paymentEntity2.setFromAccount(ACCOUNT_NUMBER_1);
        paymentEntity2.setRecipient(recipientEntity);
        paymentEntity2.setReference("37578936060100475");

        assertNotEquals(paymentEntity1.calculateHash(), paymentEntity2.calculateHash());
    }
}
