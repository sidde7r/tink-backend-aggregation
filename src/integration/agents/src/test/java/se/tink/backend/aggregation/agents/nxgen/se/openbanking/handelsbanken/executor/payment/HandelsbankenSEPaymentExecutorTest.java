package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.executor.payment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.DateValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.ReferenceValidationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.enums.HandelsbankenPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class HandelsbankenSEPaymentExecutorTest {
    private static final String CURRENCY = "SEK";

    private HandelsbankenSEPaymentExecutor paymentExecutor;
    private HandelsbankenBaseApiClient apiClient;

    private static final SwedishIdentifier ACCOUNT_SHB_1 =
            (SwedishIdentifier)
                    AccountIdentifier.create(
                            AccountIdentifierType.SE, "6666123456789", "Sven Svensson");
    private static final SwedishIdentifier ACCOUNT_SHB_2 =
            (SwedishIdentifier)
                    AccountIdentifier.create(
                            AccountIdentifierType.SE, "66661111111", "Test Testsson");
    private static final SwedishIdentifier ACCOUNT_SHB_LONG_NAME =
            (SwedishIdentifier)
                    AccountIdentifier.create(
                            AccountIdentifierType.SE,
                            "66661111112",
                            "Very long mame that will not be valid");
    private static final SwedishIdentifier ACCOUNT_SEB =
            (SwedishIdentifier)
                    AccountIdentifier.create(
                            AccountIdentifierType.SE, "912022222222", "Test Testsson");
    private static final PlusGiroIdentifier ACCOUNT_PLUSGIRO =
            new PlusGiroIdentifier("9020900", "1212121212");

    private static final String REF_12_CHARS = "From Sven S.";
    private static final String REF_14_CHARS = "Payment Sven S";
    private static final String REF_17_CHARS = "Payment From Sven";
    private static final String REF_211_CHARS =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque lorem velit, pharetra a ante ac, tincidunt sollicitudin lectus. Integer eget gravida quam, eget commodo tortor. Nulla efficitur suscipit sed.";

    @Before
    public void setUp() {
        apiClient = mock(HandelsbankenBaseApiClient.class);
        paymentExecutor =
                new HandelsbankenSEPaymentExecutor(
                        apiClient,
                        mock(Credentials.class),
                        mock(SupplementalInformationController.class),
                        mock(PersistentStorage.class));
    }

    @Test()
    public void testPaymentToSHB() throws PaymentException {
        PaymentRequest request =
                paymentRequest(ACCOUNT_SHB_1, ACCOUNT_SHB_2, REF_14_CHARS, LocalDate.now());
        when(apiClient.createPayment(
                        any(CreatePaymentRequest.class),
                        eq(HandelsbankenPaymentType.SWEDISH_DOMESTIC_CREDIT_TRANSFER)))
                .thenReturn(new CreatePaymentResponse());
        paymentExecutor.create(request);
    }

    @Test()
    public void testPaymentToOtherBank() throws PaymentException {
        PaymentRequest request =
                paymentRequest(ACCOUNT_SHB_1, ACCOUNT_SEB, REF_12_CHARS, LocalDate.now());
        when(apiClient.createPayment(
                        any(CreatePaymentRequest.class),
                        eq(HandelsbankenPaymentType.SWEDISH_DOMESTIC_CREDIT_TRANSFER)))
                .thenReturn(new CreatePaymentResponse());
        paymentExecutor.create(request);
    }

    @Test()
    public void testPaymentToPlusGiro() throws PaymentException {
        PaymentRequest request =
                paymentRequest(ACCOUNT_SHB_1, ACCOUNT_PLUSGIRO, REF_17_CHARS, LocalDate.now());

        when(apiClient.createPayment(
                        any(CreatePaymentRequest.class),
                        eq(HandelsbankenPaymentType.SWEDISH_DOMESTIC_GIRO_PAYMENT)))
                .thenReturn(new CreatePaymentResponse());
        paymentExecutor.create(request);
    }

    @Test(expected = ReferenceValidationException.class)
    public void testTooLongRefSHB() throws PaymentException {
        PaymentRequest request =
                paymentRequest(ACCOUNT_SHB_1, ACCOUNT_SHB_2, REF_17_CHARS, LocalDate.now());
        paymentExecutor.create(request);
    }

    @Test(expected = ReferenceValidationException.class)
    public void testTooLongRefOtherBank() throws PaymentException {
        PaymentRequest request =
                paymentRequest(ACCOUNT_SHB_1, ACCOUNT_SEB, REF_14_CHARS, LocalDate.now());
        paymentExecutor.create(request);
    }

    @Test(expected = ReferenceValidationException.class)
    public void testTooLongRefGiro() throws PaymentException {
        PaymentRequest request =
                paymentRequest(ACCOUNT_SHB_1, ACCOUNT_PLUSGIRO, REF_211_CHARS, LocalDate.now());
        paymentExecutor.create(request);
    }

    @Test(expected = DateValidationException.class)
    public void testInvalidDateTransfer() throws PaymentException {
        PaymentRequest request =
                paymentRequest(
                        ACCOUNT_SHB_1, ACCOUNT_SHB_2, REF_14_CHARS, LocalDate.now().plusDays(1));
        paymentExecutor.create(request);
    }

    @Test(expected = CreditorValidationException.class)
    public void testLongCreditorNameTransfer() throws PaymentException {
        PaymentRequest request =
                paymentRequest(
                        ACCOUNT_SHB_1,
                        ACCOUNT_SHB_LONG_NAME,
                        REF_14_CHARS,
                        LocalDate.now().plusDays(1));
        paymentExecutor.create(request);
    }

    private PaymentRequest paymentRequest(
            AccountIdentifier fromAccount,
            AccountIdentifier toAccount,
            String text,
            LocalDate executionDate) {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue(text);
        return new PaymentRequest(
                new Payment.Builder()
                        .withCreditor(new Creditor(toAccount, toAccount.getName().orElse(null)))
                        .withDebtor(new Debtor(fromAccount))
                        .withExactCurrencyAmount(ExactCurrencyAmount.of(BigDecimal.ONE, CURRENCY))
                        .withExecutionDate(executionDate)
                        .withCurrency(CURRENCY)
                        .withRemittanceInformation(remittanceInformation)
                        .build());
    }
}
