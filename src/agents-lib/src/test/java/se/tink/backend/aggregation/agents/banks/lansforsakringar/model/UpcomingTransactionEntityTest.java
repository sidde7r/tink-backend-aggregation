package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import org.joda.time.DateTime;
import org.junit.Test;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class UpcomingTransactionEntityTest {
    private static final String ACCOUNT_NUMBER_1 = "01234";
    private static final String ACCOUNT_NUMBER_2 = "56789";

    @Test
    public void getHash_whenFirstFourAmountDecimalsEqual_hashesEquals() {
        RecipientEntity recipientEntity = new RecipientEntity();
        recipientEntity.setGiroNumber(new BankGiroIdentifier("7308596").toUriAsString());
        recipientEntity.setReference("37578936060100475");

        UpcomingTransactionEntity upcomingTransaction1 = new UpcomingTransactionEntity();
        upcomingTransaction1.setAmount(1.00001);
        upcomingTransaction1.setDate(new DateTime().withTimeAtStartOfDay().toDate());
        upcomingTransaction1.setPaymentInfo(recipientEntity);

        UpcomingTransactionEntity upcomingTransaction2 = new UpcomingTransactionEntity();
        upcomingTransaction2.setAmount(1.0);
        upcomingTransaction2.setDate(new DateTime().withTimeAtStartOfDay().toDate());
        upcomingTransaction2.setPaymentInfo(recipientEntity);

        assertEquals(
                upcomingTransaction1.calculatePaymentHash(ACCOUNT_NUMBER_1),
                upcomingTransaction2.calculatePaymentHash(ACCOUNT_NUMBER_1));
    }

    @Test
    public void getHash_whenAnyOfFirstFourDecimalsDiffer_hashesDiffer() {
        RecipientEntity recipientEntity = new RecipientEntity();
        recipientEntity.setGiroNumber(new BankGiroIdentifier("7308596").toUriAsString());
        recipientEntity.setReference("37578936060100475");

        UpcomingTransactionEntity upcomingTransaction1 = new UpcomingTransactionEntity();
        upcomingTransaction1.setAmount(1.0001);
        upcomingTransaction1.setDate(new DateTime().withTimeAtStartOfDay().toDate());
        upcomingTransaction1.setPaymentInfo(recipientEntity);

        UpcomingTransactionEntity upcomingTransaction2 = new UpcomingTransactionEntity();
        upcomingTransaction2.setAmount(1.0);
        upcomingTransaction2.setDate(new DateTime().withTimeAtStartOfDay().toDate());
        upcomingTransaction2.setPaymentInfo(recipientEntity);

        assertNotEquals(
                upcomingTransaction1.calculatePaymentHash(ACCOUNT_NUMBER_1),
                upcomingTransaction2.calculatePaymentHash(ACCOUNT_NUMBER_1));
    }

    @Test
    public void getHash_whenGiroNumberDiffer_hashesDiffer() {
        RecipientEntity recipientEntity = new RecipientEntity();
        recipientEntity.setGiroNumber(new BankGiroIdentifier("7308595").toUriAsString());
        recipientEntity.setReference("37578936060100475");

        UpcomingTransactionEntity upcomingTransaction1 = new UpcomingTransactionEntity();
        upcomingTransaction1.setAmount(1.0);
        upcomingTransaction1.setDate(new DateTime().withTimeAtStartOfDay().toDate());
        upcomingTransaction1.setPaymentInfo(recipientEntity);

        RecipientEntity recipientEntity2 = new RecipientEntity();
        recipientEntity2.setGiroNumber(new BankGiroIdentifier("7308596").toUriAsString());
        recipientEntity2.setReference("37578936060100475");

        UpcomingTransactionEntity upcomingTransaction2 = new UpcomingTransactionEntity();
        upcomingTransaction2.setAmount(1.0);
        upcomingTransaction2.setDate(new DateTime().withTimeAtStartOfDay().toDate());
        upcomingTransaction2.setPaymentInfo(recipientEntity2);

        assertNotEquals(
                upcomingTransaction1.calculatePaymentHash(ACCOUNT_NUMBER_1),
                upcomingTransaction2.calculatePaymentHash(ACCOUNT_NUMBER_1));
    }

    @Test
    public void getHash_whenReferenceDiffer_hashesDiffer() {
        RecipientEntity recipientEntity = new RecipientEntity();
        recipientEntity.setGiroNumber(new BankGiroIdentifier("7308595").toUriAsString());
        recipientEntity.setReference("37578936060100475");

        UpcomingTransactionEntity upcomingTransaction1 = new UpcomingTransactionEntity();
        upcomingTransaction1.setAmount(1.0);
        upcomingTransaction1.setDate(new DateTime().withTimeAtStartOfDay().toDate());
        upcomingTransaction1.setPaymentInfo(recipientEntity);

        RecipientEntity recipientEntity2 = new RecipientEntity();
        recipientEntity2.setGiroNumber(new BankGiroIdentifier("7308595").toUriAsString());
        recipientEntity2.setReference("37578936060100476");

        UpcomingTransactionEntity upcomingTransaction2 = new UpcomingTransactionEntity();
        upcomingTransaction2.setAmount(1.0);
        upcomingTransaction2.setDate(new DateTime().withTimeAtStartOfDay().toDate());
        upcomingTransaction2.setPaymentInfo(recipientEntity2);

        assertNotEquals(
                upcomingTransaction1.calculatePaymentHash(ACCOUNT_NUMBER_1),
                upcomingTransaction2.calculatePaymentHash(ACCOUNT_NUMBER_1));
    }

    @Test
    public void getHash_whenAccountNumberDiffer_hashesDiffer() {
        RecipientEntity recipientEntity = new RecipientEntity();
        recipientEntity.setGiroNumber(new BankGiroIdentifier("7308596").toUriAsString());
        recipientEntity.setReference("37578936060100475");

        UpcomingTransactionEntity upcomingTransaction1 = new UpcomingTransactionEntity();
        upcomingTransaction1.setAmount(1.0);
        upcomingTransaction1.setDate(new DateTime().withTimeAtStartOfDay().toDate());
        upcomingTransaction1.setPaymentInfo(recipientEntity);

        UpcomingTransactionEntity upcomingTransaction2 = new UpcomingTransactionEntity();
        upcomingTransaction2.setAmount(1.0);
        upcomingTransaction2.setDate(new DateTime().withTimeAtStartOfDay().toDate());
        upcomingTransaction2.setPaymentInfo(recipientEntity);

        assertNotEquals(
                upcomingTransaction1.calculatePaymentHash(ACCOUNT_NUMBER_1),
                upcomingTransaction2.calculatePaymentHash(ACCOUNT_NUMBER_2));
    }
}
