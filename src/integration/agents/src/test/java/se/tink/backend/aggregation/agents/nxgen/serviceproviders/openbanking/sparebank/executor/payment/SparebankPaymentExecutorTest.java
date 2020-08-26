package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import junitparams.JUnitParamsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.enums.SparebankPaymentProduct;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class SparebankPaymentExecutorTest {

    private static final String CURRENCY = "NOK";
    private static final String PAYMENT_UNIQUE_ID = "1234asdf";
    private static final String PAYMENT_STATUS = "ACCP";
    private static final String REFERENCE = "Short ref";
    private static final ExactCurrencyAmount AMOUNT_ONE =
            ExactCurrencyAmount.of(BigDecimal.ONE, CURRENCY);
    private static final LocalDate SOME_DATE = LocalDate.now();

    private static final String BBAN_FIRST = "97105048304";
    private static final String BBAN_SECOND = "89369733295";
    private static final String IBAN_FIRST = "NL70INGB2221802675";
    private static final String IBAN_SECOND = "SA1322321426289369733295";

    private static final AccountIdentifier ACCOUNT_NORWAY_FIRST =
            AccountIdentifier.create(AccountIdentifier.Type.NO, BBAN_FIRST, "Tut Tutennen");
    private static final AccountIdentifier ACCOUNT_NORWAY_SECOND =
            AccountIdentifier.create(AccountIdentifier.Type.NO, BBAN_SECOND, "Mop Pommomem");
    private static final AccountIdentifier ACCOUNT_IBAN_FIRST =
            AccountIdentifier.create(AccountIdentifier.Type.IBAN, IBAN_FIRST, "Iban Ibanovic");
    private static final AccountIdentifier ACCOUNT_IBAN_SECOND =
            AccountIdentifier.create(AccountIdentifier.Type.IBAN, IBAN_SECOND, "Ibanka McIban");

    private SparebankApiClient apiClient;
    private SparebankPaymentExecutor paymentExecutor;

    @Before
    public void setup() {
        apiClient = mock(SparebankApiClient.class);
        paymentExecutor =
                new SparebankPaymentExecutor(
                        apiClient, mock(SessionStorage.class), mock(SparebankConfiguration.class));
    }

    @Test
    public void shouldReturnMatchingDataAfterSchedulingDomesticPayment() {
        when(apiClient.createPayment(
                        eq(SparebankPaymentProduct.NORWEGIAN_DOMESTIC_CREDIT_TRANSFER.getText()),
                        any(CreatePaymentRequest.class)))
                .thenReturn(getCreatePaymentResponse());

        PaymentResponse paymentResponse =
                paymentExecutor.create(
                        getPaymentRequestForCreatingPayment(
                                ACCOUNT_NORWAY_FIRST, ACCOUNT_NORWAY_SECOND));

        Payment innerPaymentResponse = paymentResponse.getPayment();
        assertInputDataMatchesCreatePaymentResponse(
                ACCOUNT_NORWAY_FIRST,
                ACCOUNT_NORWAY_SECOND,
                PaymentType.DOMESTIC,
                innerPaymentResponse);
    }

    @Test
    public void shouldReturnMatchingDataAfterSchedulingSepaPayment() {
        when(apiClient.createPayment(
                        eq(SparebankPaymentProduct.SEPA_CREDIT_TRANSFER.getText()),
                        any(CreatePaymentRequest.class)))
                .thenReturn(getCreatePaymentResponse());

        PaymentResponse paymentResponse =
                paymentExecutor.create(
                        getPaymentRequestForCreatingPayment(
                                ACCOUNT_IBAN_FIRST, ACCOUNT_IBAN_SECOND));

        Payment innerPaymentResponse = paymentResponse.getPayment();
        assertInputDataMatchesCreatePaymentResponse(
                ACCOUNT_IBAN_FIRST, ACCOUNT_IBAN_SECOND, PaymentType.SEPA, innerPaymentResponse);
    }

    private void assertInputDataMatchesCreatePaymentResponse(
            AccountIdentifier expectedCreditor,
            AccountIdentifier expectedDebtor,
            PaymentType expectedType,
            Payment responsePayment) {
        assertEquals(
                expectedCreditor.getIdentifier(), responsePayment.getCreditor().getAccountNumber());
        assertEquals(
                expectedCreditor.getType(),
                responsePayment.getCreditor().getAccountIdentifierType());
        assertEquals(expectedCreditor.getName().get(), responsePayment.getCreditor().getName());
        assertEquals(
                expectedDebtor.getIdentifier(), responsePayment.getDebtor().getAccountNumber());
        assertEquals(AMOUNT_ONE, responsePayment.getExactCurrencyAmount());
        assertEquals(SOME_DATE, responsePayment.getExecutionDate());
        assertEquals(CURRENCY, responsePayment.getCurrency());
        assertEquals(PAYMENT_UNIQUE_ID, responsePayment.getUniqueId());
        assertEquals(PaymentStatus.PENDING, responsePayment.getStatus());
        assertEquals(expectedType, responsePayment.getType());
    }

    @Test
    public void shouldThrowWhenTryingToScheduleInernationalPayment() {
        Throwable throwable =
                catchThrowable(
                        () ->
                                paymentExecutor.create(
                                        getPaymentRequestForCreatingPayment(
                                                ACCOUNT_IBAN_FIRST, ACCOUNT_NORWAY_SECOND)));
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(SparebankConstants.ErrorMessages.INTERNATIONAL_TRANFER_NOT_SUPPORTED);
    }

    @Test
    public void shouldThrowWhenTryingToFetchDomesticPayment() {
        Throwable throwable =
                catchThrowable(
                        () ->
                                paymentExecutor.fetch(
                                        getPaymentRequestForFetchingPayment(PaymentType.DOMESTIC)));
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(SparebankConstants.ErrorMessages.DOMESTIC_FETCHING_NOT_SUPPORTED);
    }

    @Test
    public void shouldMapDataCorrectlyWhenFetchingSepaPayment() {
        PaymentRequest paymentRequest = getPaymentRequestForFetchingPayment(PaymentType.SEPA);

        when(apiClient.getPayment(
                        eq(SparebankPaymentProduct.SEPA_CREDIT_TRANSFER.getText()),
                        eq(PAYMENT_UNIQUE_ID)))
                .thenReturn(getGetPaymentResponse());

        PaymentResponse paymentResponse = paymentExecutor.fetch(paymentRequest);
        Payment innerPaymentResponse = paymentResponse.getPayment();

        assertFetchPaymentMapsCorrectly(
                ACCOUNT_IBAN_FIRST, ACCOUNT_IBAN_SECOND, PaymentType.SEPA, innerPaymentResponse);
    }

    @Test
    public void shouldMapDataCorrectlyWhenFetchingInternationalPayment() {
        PaymentRequest paymentRequest =
                getPaymentRequestForFetchingPayment(PaymentType.INTERNATIONAL);

        when(apiClient.getPayment(
                        eq(SparebankPaymentProduct.CROSS_BORDER_CREDIT_TRANSFER.getText()),
                        eq(PAYMENT_UNIQUE_ID)))
                .thenReturn(getGetPaymentResponse());

        PaymentResponse paymentResponse = paymentExecutor.fetch(paymentRequest);
        Payment innerPaymentResponse = paymentResponse.getPayment();
        assertFetchPaymentMapsCorrectly(
                ACCOUNT_IBAN_FIRST,
                ACCOUNT_IBAN_SECOND,
                PaymentType.INTERNATIONAL,
                innerPaymentResponse);
    }

    private void assertFetchPaymentMapsCorrectly(
            AccountIdentifier expectedCreditor,
            AccountIdentifier expectedDebtor,
            PaymentType expectedType,
            Payment responsePayment) {
        assertEquals(
                expectedCreditor.getIdentifier(), responsePayment.getCreditor().getAccountNumber());
        assertEquals(
                expectedCreditor.getType(),
                responsePayment.getCreditor().getAccountIdentifierType());
        assertEquals(
                expectedDebtor.getIdentifier(), responsePayment.getDebtor().getAccountNumber());
        assertEquals(
                expectedDebtor.getType(), responsePayment.getDebtor().getAccountIdentifierType());
        assertEquals(AMOUNT_ONE, responsePayment.getExactCurrencyAmount());
        assertEquals(CURRENCY, responsePayment.getCurrency());
        assertEquals(PAYMENT_UNIQUE_ID, responsePayment.getUniqueId());
        assertEquals(PaymentStatus.PENDING, responsePayment.getStatus());
        assertEquals(expectedType, responsePayment.getType());
    }

    private PaymentRequest getPaymentRequestForCreatingPayment(
            AccountIdentifier creditorIdentifier, AccountIdentifier debtorIdentifier) {
        return new PaymentRequest(
                new Payment.Builder()
                        .withCreditor(
                                new Creditor(
                                        creditorIdentifier,
                                        creditorIdentifier.getName().orElse(null)))
                        .withDebtor(new Debtor(debtorIdentifier))
                        .withExactCurrencyAmount(AMOUNT_ONE)
                        .withExecutionDate(SOME_DATE)
                        .withCurrency(CURRENCY)
                        .withReference(new Reference(null, REFERENCE))
                        .build());
    }

    private PaymentRequest getPaymentRequestForFetchingPayment(PaymentType type) {
        return new PaymentRequest(
                new Payment.Builder().withUniqueId(PAYMENT_UNIQUE_ID).withType(type).build());
    }

    private CreatePaymentResponse getCreatePaymentResponse() {
        return SerializationUtils.deserializeFromString(
                "{\"paymentId\": \""
                        + PAYMENT_UNIQUE_ID
                        + "\", \"transactionStatus\": \""
                        + PAYMENT_STATUS
                        + "\"}",
                CreatePaymentResponse.class);
    }

    private GetPaymentResponse getGetPaymentResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"creditorAccount\": {\n"
                        + "        \"iban\": \""
                        + IBAN_FIRST
                        + "\"\n"
                        + "    },\n"
                        + "    \"creditorName\": \"Kopytek Kopytkowicz\",\n"
                        + "    \"debtorAccount\": {\n"
                        + "        \"iban\": \""
                        + IBAN_SECOND
                        + "\"\n"
                        + "    },\n"
                        + "    \"instructedAmount\": {\n"
                        + "        \"amount\": \""
                        + AMOUNT_ONE.getDoubleValue()
                        + "\",\n"
                        + "        \"currency\": \""
                        + CURRENCY
                        + "\"\n"
                        + "    }\n"
                        + "}",
                GetPaymentResponse.class);
    }
}
