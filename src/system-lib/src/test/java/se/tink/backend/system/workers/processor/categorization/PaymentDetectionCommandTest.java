package se.tink.backend.system.workers.processor.categorization;

import com.google.inject.Inject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.system.workers.processor.other.payment.PaymentDetectionCommand;
import se.tink.backend.util.GuiceRunner;
import se.tink.backend.util.TestUtil;

@RunWith(GuiceRunner.class)
public class PaymentDetectionCommandTest {
    @Inject
    private TestUtil testUtil;
    
    @Test
    public void testTransactionModifiedDescription(){

        Transaction transaction = testUtil.getNewTransaction("", -334, "test");
        transaction.setUserModifiedDescription(true);

        new PaymentDetectionCommand().execute(transaction);

        boolean expModifiedDescription = true;
        String expDescription = "test";

        Assert.assertEquals("Transaction modifier was changed", expModifiedDescription, transaction.isUserModifiedDescription());
        Assert.assertEquals("Transaction description was changed", expDescription, transaction.getDescription());
    }

    @Test
    public void testTransactionWithNonPaymentPrefix(){
        Transaction transaction = testUtil.getNewTransaction("", -334, "izettle abc");

        new PaymentDetectionCommand().execute(transaction);

        boolean expModifiedDescription = false;
        String expDescription = "izettle abc";

        Assert.assertEquals("Transaction modifier was changed", expModifiedDescription, transaction.isUserModifiedDescription());
        Assert.assertEquals("Transaction description was changed", expDescription, transaction.getDescription());
    }

    @Test
    public void testTransactionWithNonPaymentPrefixAndPaymentGateway(){
        Transaction transaction = testUtil.getNewTransaction("", -334, "izettle abc");
        transaction.setPayload(TransactionPayloadTypes.PAYMENT_GATEWAY, "izettle ab");

        new PaymentDetectionCommand().execute(transaction);

        boolean expModifiedDescription = false;
        String expDescription = "izettle abc";
        boolean expPaymentGateway = false;

        Assert.assertEquals("Transaction modifier was changed", expModifiedDescription, transaction.isUserModifiedDescription());
        Assert.assertEquals("Transaction description was changed", expDescription, transaction.getDescription());
        Assert.assertEquals("Transaction payment gateway should be reset", expPaymentGateway, transaction.getPayload().containsKey(TransactionPayloadTypes.PAYMENT_GATEWAY));
    }

    @Test
    public void testTransactioWithNonPaymentDescription(){

        Transaction transaction = testUtil.getNewTransaction("", -334, "test");

        new PaymentDetectionCommand().execute(transaction);

        boolean expModifiedDescription = false;
        String expDescription = "test";

        Assert.assertEquals("Transaction modifier was changed", expModifiedDescription, transaction.isUserModifiedDescription());
        Assert.assertEquals("Transaction description was changed", expDescription, transaction.getDescription());
    }

    @Test
    public void testTransactionWithSameDescription(){
        Transaction transaction = testUtil.getNewTransaction("", -334, "paypal ");

        new PaymentDetectionCommand().execute(transaction);

        boolean expModifiedDescription = false;
        String expDescription = "Paypal";
        boolean expPaymentGateway = true;
        String expPaymentValue = "Paypal";

        Assert.assertEquals("Transaction modifier was changed", expModifiedDescription, transaction.isUserModifiedDescription());
        Assert.assertEquals("Transaction description was changed", expDescription, transaction.getDescription());
        Assert.assertEquals("Transaction payment gateway should be added", expPaymentGateway, transaction.getPayload().containsKey(TransactionPayloadTypes.PAYMENT_GATEWAY));
        Assert.assertEquals("Wrong payment value", expPaymentValue, transaction.getPayload().get(TransactionPayloadTypes.PAYMENT_GATEWAY));
    }

    @Test
    public void testTransactionWithSameDescriptionAndPaymentGateway(){
        Transaction transaction = testUtil.getNewTransaction("", -334, "paypal ");
        transaction.setPayload(TransactionPayloadTypes.PAYMENT_GATEWAY, "paypal");

        new PaymentDetectionCommand().execute(transaction);

        boolean expModifiedDescription = false;
        String expDescription = "Paypal";
        boolean expPaymentGateway = true;
        String expPaymentValue = "Paypal";

        Assert.assertEquals("Transaction modifier was changed", expModifiedDescription, transaction.isUserModifiedDescription());
        Assert.assertEquals("Transaction description was changed", expDescription, transaction.getDescription());
        Assert.assertEquals("Transaction payment gateway should be added", expPaymentGateway, transaction.getPayload().containsKey(TransactionPayloadTypes.PAYMENT_GATEWAY));
        Assert.assertEquals("Wrong payment value", expPaymentValue, transaction.getPayload().get(TransactionPayloadTypes.PAYMENT_GATEWAY));
    }

    @Test
    public void testTransactionWithExtendedDescription(){
        Transaction transaction = testUtil.getNewTransaction("", -334, "paypal Tink");

        new PaymentDetectionCommand().execute(transaction);

        boolean expModifiedDescription = false;
        String expDescription = "Tink";
        boolean expPaymentGateway = true;
        String expPaymentValue = "Paypal";

        Assert.assertEquals("Transaction modifier was changed", expModifiedDescription, transaction.isUserModifiedDescription());
        Assert.assertEquals("Transaction description was changed", expDescription, transaction.getDescription());
        Assert.assertEquals("Transaction payment gateway should be added", expPaymentGateway, transaction.getPayload().containsKey(TransactionPayloadTypes.PAYMENT_GATEWAY));
        Assert.assertEquals("Wrong payment value", expPaymentValue, transaction.getPayload().get(TransactionPayloadTypes.PAYMENT_GATEWAY));
    }

    @Test
    public void testTransactionWithExtendedDescriptionAndPaymentGateway(){
        Transaction transaction = testUtil.getNewTransaction("", -334, "paypal Tink");
        transaction.setPayload(TransactionPayloadTypes.PAYMENT_GATEWAY, "paypal");

        new PaymentDetectionCommand().execute(transaction);

        boolean expModifiedDescription = false;
        String expDescription = "Tink";
        boolean expPaymentGateway = true;
        String expPaymentValue = "Paypal";

        Assert.assertEquals("Transaction modifier was changed", expModifiedDescription, transaction.isUserModifiedDescription());
        Assert.assertEquals("Transaction description was changed", expDescription, transaction.getDescription());
        Assert.assertEquals("Transaction payment gateway should be added", expPaymentGateway, transaction.getPayload().containsKey(TransactionPayloadTypes.PAYMENT_GATEWAY));
        Assert.assertEquals("Wrong payment value", expPaymentValue, transaction.getPayload().get(TransactionPayloadTypes.PAYMENT_GATEWAY));
    }
}
