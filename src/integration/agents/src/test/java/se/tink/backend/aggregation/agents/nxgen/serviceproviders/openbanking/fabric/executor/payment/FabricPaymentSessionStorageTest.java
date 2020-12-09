package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment;

import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;

public class FabricPaymentSessionStorageTest {

    @Test
    public void testDefaultProduct() {
        FabricPaymentSessionStorage sut = new FabricPaymentSessionStorage(new SessionStorage());
        Assert.assertEquals(
                sut.get(FabricConstants.PathParameterKeys.PAYMENT_PRODUCT),
                FabricConstants.PathParameterValues.PAYMENT_PRODUCT_SEPA_CREDIT);
    }

    @Test
    public void testDefaultPayment() {
        FabricPaymentSessionStorage sut = new FabricPaymentSessionStorage(new SessionStorage());
        Payment payment =
                new Payment.Builder()
                        .withUniqueId(UUID.randomUUID().toString().substring(5))
                        .build();
        sut.updatePaymentProductIfNeeded(payment);
        Assert.assertEquals(
                sut.get(FabricConstants.PathParameterKeys.PAYMENT_PRODUCT),
                FabricConstants.PathParameterValues.PAYMENT_PRODUCT_SEPA_CREDIT);
    }

    @Test
    public void testSepaInstantPayment() {
        FabricPaymentSessionStorage sut = new FabricPaymentSessionStorage(new SessionStorage());
        Payment payment =
                new Payment.Builder()
                        .withUniqueId(UUID.randomUUID().toString().substring(5))
                        .withPaymentScheme(PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER)
                        .build();
        sut.updatePaymentProductIfNeeded(payment);
        Assert.assertEquals(
                sut.get(FabricConstants.PathParameterKeys.PAYMENT_PRODUCT),
                FabricConstants.PathParameterValues.PAYMENT_PRODUCT_SEPA_INSTANT);
    }

    @Test
    public void testSepaCreditPayment() {
        FabricPaymentSessionStorage sut = new FabricPaymentSessionStorage(new SessionStorage());
        Payment payment =
                new Payment.Builder()
                        .withUniqueId(UUID.randomUUID().toString().substring(5))
                        .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                        .build();
        sut.updatePaymentProductIfNeeded(payment);
        Assert.assertEquals(
                sut.get(FabricConstants.PathParameterKeys.PAYMENT_PRODUCT),
                FabricConstants.PathParameterValues.PAYMENT_PRODUCT_SEPA_CREDIT);
    }
}
