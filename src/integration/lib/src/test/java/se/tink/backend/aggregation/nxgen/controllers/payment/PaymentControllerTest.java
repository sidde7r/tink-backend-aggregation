package se.tink.backend.aggregation.nxgen.controllers.payment;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class PaymentControllerTest {

    @Rule public ExpectedException exception = ExpectedException.none();

    private PaymentController paymentController;
    private final String DESTINATION_IDENTIFIER = "90247471978";
    private final String SOURCE_IDENTIFIER_BBAN = "90247471978";
    private final String SOURCE_IDENTIFIER_IBAN_SE = "SE7280000810340009783242";
    private final String SOURCE_IDENTIFIER_IBAN_GB = "GB33BUKB20201555555555";

    @Test
    public void testGetPaymentProductTypeForBbanToBbanIsDomestic() {
        Payment payment =
                new Payment.Builder()
                        .withAmount(Amount.inSEK(1.0))
                        .withCreditor(
                                new Creditor(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.BBAN,
                                                DESTINATION_IDENTIFIER),
                                        "Test Person"))
                        .withDebtor(
                                new Debtor(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.BBAN,
                                                SOURCE_IDENTIFIER_BBAN)))
                        .withCurrency("SEK")
                        .withExecutionDate(LocalDate.now())
                        .withRemittanceInformation(
                                createAndGetRemittanceInformation(null, "random"))
                        .build();

        paymentController = new PaymentController(mock(PaymentExecutor.class));
        PaymentType paymentType = paymentController.getPaymentProductType(payment);

        assertEquals(paymentType, PaymentType.DOMESTIC);
    }

    @Test
    public void testGetPaymentProductTypeForBbanToIbanisDomesticFuture() {

        Payment payment =
                new Payment.Builder()
                        .withAmount(Amount.inSEK(1.0))
                        .withCreditor(
                                new Creditor(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.IBAN,
                                                DESTINATION_IDENTIFIER),
                                        "Test Person"))
                        .withDebtor(
                                new Debtor(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.BBAN,
                                                SOURCE_IDENTIFIER_BBAN)))
                        .withCurrency("SEK")
                        .withExecutionDate(LocalDate.now().plusDays(2))
                        .withRemittanceInformation(
                                createAndGetRemittanceInformation(null, "random"))
                        .build();

        paymentController = new PaymentController(mock(PaymentExecutor.class));
        PaymentType paymentType = paymentController.getPaymentProductType(payment);

        assertEquals(paymentType, PaymentType.DOMESTIC_FUTURE);
    }

    @Test
    public void testGetPaymentProductTypeForIbanToIbanSEisDomestic() {

        Payment payment =
                new Payment.Builder()
                        .withAmount(Amount.inSEK(1.0))
                        .withCreditor(
                                new Creditor(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.IBAN,
                                                DESTINATION_IDENTIFIER),
                                        "Test Person"))
                        .withDebtor(
                                new Debtor(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.IBAN,
                                                SOURCE_IDENTIFIER_IBAN_SE)))
                        .withCurrency("SEK")
                        .withExecutionDate(LocalDate.now())
                        .withRemittanceInformation(
                                createAndGetRemittanceInformation(null, "random"))
                        .build();

        paymentController = new PaymentController(mock(PaymentExecutor.class));
        PaymentType paymentType = paymentController.getPaymentProductType(payment);

        assertEquals(paymentType, PaymentType.DOMESTIC);
    }

    @Test
    public void testGetPaymentProductTypeForIbanToIbanGBisInternational() {

        Payment payment =
                new Payment.Builder()
                        .withAmount(Amount.inEUR(1.0))
                        .withCreditor(
                                new Creditor(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.IBAN,
                                                DESTINATION_IDENTIFIER),
                                        "Test Person"))
                        .withDebtor(
                                new Debtor(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.IBAN,
                                                SOURCE_IDENTIFIER_IBAN_GB)))
                        .withCurrency("£")
                        .withExecutionDate(LocalDate.now())
                        .withRemittanceInformation(
                                createAndGetRemittanceInformation(null, "random"))
                        .build();

        paymentController = new PaymentController(mock(PaymentExecutor.class));
        PaymentType paymentType = paymentController.getPaymentProductType(payment);

        assertEquals(paymentType, PaymentType.INTERNATIONAL);
    }

    @Test
    public void testGetPaymentProductTypeForUnmappedCombination() {
        exception.expect(IllegalStateException.class);

        Payment payment =
                new Payment.Builder()
                        .withAmount(Amount.inEUR(1.0))
                        .withCreditor(
                                new Creditor(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.IBAN,
                                                DESTINATION_IDENTIFIER),
                                        "Test Person"))
                        .withDebtor(
                                new Debtor(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.BE,
                                                SOURCE_IDENTIFIER_IBAN_GB)))
                        .withCurrency("£")
                        .withExecutionDate(LocalDate.now())
                        .withRemittanceInformation(
                                createAndGetRemittanceInformation(null, "random"))
                        .build();

        paymentController = new PaymentController(mock(PaymentExecutor.class));
        paymentController.getPaymentProductType(payment);
    }

    @Test
    public void testGetPaymentProductTypeNullIdentifier() {
        exception.expect(NullPointerException.class);

        Payment payment =
                new Payment.Builder()
                        .withAmount(Amount.inSEK(1.0))
                        .withCreditor(
                                new Creditor(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.BBAN,
                                                DESTINATION_IDENTIFIER),
                                        "Test Person"))
                        .withDebtor(
                                new Debtor(
                                        AccountIdentifier.create(null, SOURCE_IDENTIFIER_IBAN_GB)))
                        .withCurrency("SEK")
                        .withExecutionDate(LocalDate.now())
                        .withRemittanceInformation(
                                createAndGetRemittanceInformation(null, "random"))
                        .build();

        paymentController = new PaymentController(mock(PaymentExecutor.class));
        paymentController.getPaymentProductType(payment);
    }

    private RemittanceInformation createAndGetRemittanceInformation(
            RemittanceInformationType type, String value) {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(type);
        remittanceInformation.setValue(value);
        return remittanceInformation;
    }
}
