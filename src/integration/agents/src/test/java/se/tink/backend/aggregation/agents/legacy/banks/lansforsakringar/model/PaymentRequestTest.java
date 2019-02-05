package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import org.joda.time.DateTime;
import org.junit.Test;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class PaymentRequestTest {
    private static final String ACCOUNT_NUMBER_1 = "01234";
    private static final String ACCOUNT_NUMBER_2 = "56789";

    @Test
    public void getHash_whenFirstFourAmountDecimalsEqual_hashesEquals() {
        PaymentRequest paymentRequest1 = new PaymentRequest();
        paymentRequest1.setAmount(1.00001);
        paymentRequest1.setToBgPg(new BankGiroIdentifier("7308596").toUriAsString());
        paymentRequest1.setReference("37578936060100475");
        paymentRequest1.setElectronicInvoiceId("");
        paymentRequest1.setFromAccount(ACCOUNT_NUMBER_1);
        paymentRequest1.setPaymentDate(new DateTime().withTimeAtStartOfDay().getMillis());

        PaymentRequest paymentRequest2 = new PaymentRequest();
        paymentRequest2.setAmount(1.0);
        paymentRequest2.setToBgPg(new BankGiroIdentifier("7308596").toUriAsString());
        paymentRequest2.setReference("37578936060100475");
        paymentRequest2.setElectronicInvoiceId("");
        paymentRequest2.setFromAccount(ACCOUNT_NUMBER_1);
        paymentRequest2.setPaymentDate(new DateTime().withTimeAtStartOfDay().getMillis());

        assertEquals(paymentRequest1.calculateHash(), paymentRequest2.calculateHash());
    }

    @Test
    public void getHash_whenAnyOfFirstFourDecimalsDiffer_hashesDiffer() {
        PaymentRequest paymentRequest1 = new PaymentRequest();
        paymentRequest1.setAmount(1.0001);
        paymentRequest1.setToBgPg(new BankGiroIdentifier("7308596").toUriAsString());
        paymentRequest1.setReference("37578936060100475");
        paymentRequest1.setElectronicInvoiceId("");
        paymentRequest1.setFromAccount(ACCOUNT_NUMBER_1);
        paymentRequest1.setPaymentDate(new DateTime().withTimeAtStartOfDay().getMillis());

        PaymentRequest paymentRequest2 = new PaymentRequest();
        paymentRequest2.setAmount(1.0);
        paymentRequest2.setToBgPg(new BankGiroIdentifier("7308596").toUriAsString());
        paymentRequest2.setReference("37578936060100475");
        paymentRequest2.setElectronicInvoiceId("");
        paymentRequest2.setFromAccount(ACCOUNT_NUMBER_1);
        paymentRequest2.setPaymentDate(new DateTime().withTimeAtStartOfDay().getMillis());

        assertNotEquals(paymentRequest1.calculateHash(), paymentRequest2.calculateHash());
    }

    @Test
    public void getHash_whenGiroNumberDiffer_hashesDiffer() {
        PaymentRequest paymentRequest1 = new PaymentRequest();
        paymentRequest1.setAmount(1.0);
        paymentRequest1.setToBgPg(new BankGiroIdentifier("7308597").toUriAsString());
        paymentRequest1.setReference("37578936060100475");
        paymentRequest1.setElectronicInvoiceId("");
        paymentRequest1.setFromAccount(ACCOUNT_NUMBER_1);
        paymentRequest1.setPaymentDate(new DateTime().withTimeAtStartOfDay().getMillis());

        PaymentRequest paymentRequest2 = new PaymentRequest();
        paymentRequest2.setAmount(1.0);
        paymentRequest2.setToBgPg(new BankGiroIdentifier("7308596").toUriAsString());
        paymentRequest2.setReference("37578936060100475");
        paymentRequest2.setElectronicInvoiceId("");
        paymentRequest2.setFromAccount(ACCOUNT_NUMBER_1);
        paymentRequest2.setPaymentDate(new DateTime().withTimeAtStartOfDay().getMillis());

        assertNotEquals(paymentRequest1.calculateHash(), paymentRequest2.calculateHash());
    }

    @Test
    public void getHash_whenReferenceDiffer_hashesDiffer() {
        PaymentRequest paymentRequest1 = new PaymentRequest();
        paymentRequest1.setAmount(1.0);
        paymentRequest1.setToBgPg(new BankGiroIdentifier("7308596").toUriAsString());
        paymentRequest1.setReference("37578936060100476");
        paymentRequest1.setElectronicInvoiceId("");
        paymentRequest1.setFromAccount(ACCOUNT_NUMBER_1);
        paymentRequest1.setPaymentDate(new DateTime().withTimeAtStartOfDay().getMillis());

        PaymentRequest paymentRequest2 = new PaymentRequest();
        paymentRequest2.setAmount(1.0);
        paymentRequest2.setToBgPg(new BankGiroIdentifier("7308596").toUriAsString());
        paymentRequest2.setReference("37578936060100475");
        paymentRequest2.setElectronicInvoiceId("");
        paymentRequest2.setFromAccount(ACCOUNT_NUMBER_1);
        paymentRequest2.setPaymentDate(new DateTime().withTimeAtStartOfDay().getMillis());

        assertNotEquals(paymentRequest1.calculateHash(), paymentRequest2.calculateHash());
    }

    @Test
    public void getHash_whenAccountNumberDiffer_hashesDiffer() {
        PaymentRequest paymentRequest1 = new PaymentRequest();
        paymentRequest1.setAmount(1.0);
        paymentRequest1.setToBgPg(new BankGiroIdentifier("7308596").toUriAsString());
        paymentRequest1.setReference("37578936060100475");
        paymentRequest1.setElectronicInvoiceId("");
        paymentRequest1.setFromAccount(ACCOUNT_NUMBER_1);
        paymentRequest1.setPaymentDate(new DateTime().withTimeAtStartOfDay().getMillis());

        PaymentRequest paymentRequest2 = new PaymentRequest();
        paymentRequest2.setAmount(1.0);
        paymentRequest2.setToBgPg(new BankGiroIdentifier("7308596").toUriAsString());
        paymentRequest2.setReference("37578936060100475");
        paymentRequest2.setElectronicInvoiceId("");
        paymentRequest2.setFromAccount(ACCOUNT_NUMBER_2);
        paymentRequest2.setPaymentDate(new DateTime().withTimeAtStartOfDay().getMillis());

        assertNotEquals(paymentRequest1.calculateHash(), paymentRequest2.calculateHash());
    }
}
