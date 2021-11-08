package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import agents_platform_agents_framework.org.springframework.test.util.ReflectionTestUtils;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import javax.ws.rs.core.MediaType;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.entities.PaymentSourceAccount;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.rpc.PaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.rpc.PaymentSourceAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.utils.SkandiaBankenDateUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

@RunWith(MockitoJUnitRunner.class)
public class SkandiaBankenPaymentExecutorTest {
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Stockholm");

    private SkandiaBankenPaymentExecutor objectUnderTest;
    private SkandiaBankenApiClient apiClient;

    @Mock private HttpResponse httpResponse;
    @Mock private HttpResponseException httpResponseException;

    @Before
    public void setUp() {
        apiClient = mock(SkandiaBankenApiClient.class);
        objectUnderTest =
                new SkandiaBankenPaymentExecutor(
                        apiClient, mock(SupplementalInformationController.class));
    }

    @Test
    public void shouldPassValidationWhenTransferIsValid() {
        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        objectUnderTest,
                                        "validateTransferOrThrow",
                                        getValidTransfer()));

        // then
        assertNull(thrown);
    }

    @Test
    public void shouldThrowTransferExceptionWhenDestinationIsNotBgOrPg() {
        // when
        final ThrowingCallable callable =
                () ->
                        ReflectionTestUtils.invokeMethod(
                                objectUnderTest, "throwIfNotBgOrPgPayment", getA2ATransfer());

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(TransferExecutionException.class)
                .hasMessage(
                        "Provided payment type is not supported. Only PG and BG type is supported.");
    }

    @Test
    public void shouldNotThrowTransferExceptionWhenDestinationIsBg() {
        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        objectUnderTest,
                                        "throwIfNotBgOrPgPayment",
                                        getBgTransfer()));

        // then
        assertNull(thrown);
    }

    @Test
    public void shouldNotThrowTransferExceptionWhenDestinationIsPg() {
        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        objectUnderTest,
                                        "throwIfNotBgOrPgPayment",
                                        getPgTransfer()));

        // then
        assertNull(thrown);
    }

    @Test
    public void shouldThrowTransferExceptionWhenAmountIsLessThan1Sek() {
        // when
        final ThrowingCallable callable =
                () ->
                        ReflectionTestUtils.invokeMethod(
                                objectUnderTest,
                                "throwIfAmountIsLessThanMinAmount",
                                getLessThan1SekTransfer());

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(TransferExecutionException.class)
                .hasMessage(
                        "Minimum amount of payment is 1 SEK. This is a restriction set by the bank.");
    }

    @Test
    public void shouldNotThrowTransferExceptionWhenAmountIs1Sek() {
        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        objectUnderTest,
                                        "throwIfAmountIsLessThanMinAmount",
                                        get1SekTransfer()));

        // then
        assertNull(thrown);
    }

    @Test
    public void shouldNotThrowTransferExceptionWhenAmountIsGreaterThan1Sek() {
        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        objectUnderTest,
                                        "throwIfAmountIsLessThanMinAmount",
                                        getGreaterThan1SekTransfer()));

        // then
        assertNull(thrown);
    }

    @Test
    public void shouldThrowTransferExceptionWhenUnstructuredRefIsLongerThan175Chars() {
        // given
        Transfer transfer =
                getTransferWithUnstructuredRemittanceInformation(
                        "asdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwerty");

        // when
        final ThrowingCallable callable =
                () ->
                        ReflectionTestUtils.invokeMethod(
                                objectUnderTest, "throwIfUnstructuredRefLongerThanMax", transfer);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(TransferExecutionException.class)
                .hasMessage(
                        "Unstructured reference longer than 175 chars. Bank crops reference if longer, therefore we cancel the payment.");
    }

    @Test
    public void shouldNotThrowTransferExceptionWhenUnstructuredRefIs175Chars() {
        // given
        Transfer transfer =
                getTransferWithUnstructuredRemittanceInformation(
                        "asdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfq");

        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        objectUnderTest,
                                        "throwIfUnstructuredRefLongerThanMax",
                                        transfer));

        // then
        assertNull(thrown);
    }

    @Test
    public void shouldNotThrowTransferExceptionWhenUnstructuredRefIsShorterThan175Chars() {
        // given
        Transfer transfer =
                getTransferWithUnstructuredRemittanceInformation("asdfqwertyasdfqwerty");

        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        objectUnderTest,
                                        "throwIfUnstructuredRefLongerThanMax",
                                        transfer));

        // then
        assertNull(thrown);
    }

    @Test
    public void shouldThrowTransferExceptionWhenSourceAccountNotPresentInAvailableAccounts() {
        // given
        Transfer transfer = getTransferWithSource("91592222222");
        when(apiClient.fetchPaymentSourceAccounts()).thenReturn(getPaymentSourceAccountsResponse());

        // when
        final ThrowingCallable callable =
                () ->
                        ReflectionTestUtils.invokeMethod(
                                objectUnderTest, "getPaymentSourceAccount", transfer);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(TransferExecutionException.class)
                .hasMessage("Transfer source account was not present in user's payment accounts.");
    }

    @Test
    public void shouldFindSourceAccountWhenPresentInAvailableAccounts() {
        // given
        Transfer transfer = getTransferWithSource("91599999999");
        PaymentSourceAccountsResponse paymentSourceAccountsResponse =
                getPaymentSourceAccountsResponse();
        when(apiClient.fetchPaymentSourceAccounts()).thenReturn(paymentSourceAccountsResponse);

        // when
        PaymentSourceAccount paymentSourceAccount =
                ReflectionTestUtils.invokeMethod(
                        objectUnderTest, "getPaymentSourceAccount", transfer);

        // then
        assertNotNull(paymentSourceAccount);
        assertThat(paymentSourceAccount).isEqualTo(paymentSourceAccountsResponse.get(0));
    }

    @Test
    public void shouldThrowBankIdTimeoutWhenErrorMessageContainsMobiltBankIdCollectAsync() {
        when(apiClient.pollSignStatus(anyString())).thenThrow(httpResponseException);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(500);
        when(httpResponse.getType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        when(httpResponse.getBody(ErrorResponse.class)).thenReturn(getBankIdTimeout());

        ThrowingCallable callable =
                () ->
                        ReflectionTestUtils.invokeMethod(
                                objectUnderTest, "getSignStatus", "mockedSignReference");

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(TransferExecutionException.class)
                .hasMessage("Signing of payment timed out.");
    }

    @Test
    public void shouldThrowBankSideFailureWhenErrorResponseIsApiException() {
        when(apiClient.fetchPaymentSourceAccounts()).thenThrow(httpResponseException);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        when(httpResponse.getBody(ErrorResponse.class)).thenReturn(getBankApiException());

        Transfer transfer = getTransferWithSource("91599999999");

        ThrowingCallable callable =
                () ->
                        ReflectionTestUtils.invokeMethod(
                                objectUnderTest, "getPaymentSourceAccount", transfer);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(BankServiceException.class)
                .hasMessage(
                        "Exception of type 'Helium.Api.Common.Exceptions.HeliumApiException' was thrown.");
    }

    @Test
    public void shouldThrowBankIdAnotherInProgressWhenAnotherSignIsActive() {
        when(apiClient.initSignPayment(anyString())).thenThrow(httpResponseException);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(500);
        when(httpResponse.getType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        when(httpResponse.getBody(ErrorResponse.class)).thenReturn(getBankIdAlreadyInProgress());

        ThrowingCallable callable =
                () ->
                        ReflectionTestUtils.invokeMethod(
                                objectUnderTest, "initPaymentSigning", "mockedEncryptedPaymentId");

        assertThatThrownBy(callable)
                .isInstanceOf(TransferExecutionException.class)
                .hasMessage("Another BankId authentication was initiated while authenticating.");
    }

    @Test
    public void shouldThrowTransferExceptionWhenErrorResponseIsInvalidOcr() {
        // given
        when(httpResponse.getStatus()).thenReturn(400);
        when(httpResponse.getType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);

        when(httpResponseException.getResponse()).thenReturn(httpResponse);

        when(httpResponse.getBody(ErrorResponse.class)).thenReturn(getInvalidOcrErrorResponse());

        PaymentRequest paymentRequest = mock(PaymentRequest.class);
        doThrow(httpResponseException).when(apiClient).submitPayment(paymentRequest);

        // when
        ThrowingCallable callable =
                () ->
                        ReflectionTestUtils.invokeMethod(
                                objectUnderTest, "submitPayment", paymentRequest, new Date());

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(TransferExecutionException.class)
                .hasMessage("Error could not be submitted to bank due to invalid OCR.");
    }

    @Test
    public void shouldThrowInvalidDateTransferExceptionWhen500ResponseAndPaymentDateIsInvalid() {
        // given

        // Current date and time: 2021-10-29 14:20:50 CEST
        SkandiaBankenDateUtils.setClockForTesting(
                Clock.fixed(Instant.ofEpochSecond(1635510050), ZONE_ID));

        // Payment date: 2021-10-29 00:00:00 CEST
        Date paymentDate = Date.from(Instant.ofEpochSecond(1635458400));

        setUpSubmitPayment500HttpResponse();
        PaymentRequest paymentRequest = mock(PaymentRequest.class);
        doThrow(httpResponseException).when(apiClient).submitPayment(paymentRequest);

        // when
        ThrowingCallable callable =
                () ->
                        ReflectionTestUtils.invokeMethod(
                                objectUnderTest, "submitPayment", paymentRequest, paymentDate);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(TransferExecutionException.class)
                .hasMessage("Payment could not be submitted, date was rejected by bank.");
    }

    @Test
    public void shouldThrowFailedTransferExceptionWhen500ResponseAndPaymentDateIsBeforeCutOff() {
        // given

        // Current date and time: 2021-11-01 07:15:37 CET
        SkandiaBankenDateUtils.setClockForTesting(
                Clock.fixed(Instant.ofEpochSecond(1635750937), ZONE_ID));

        // Payment date: 2021-11-01 00:00:00 CET
        Date paymentDate = Date.from(Instant.ofEpochSecond(1635724800));

        setUpSubmitPayment500HttpResponse();
        PaymentRequest paymentRequest = mock(PaymentRequest.class);
        doThrow(httpResponseException).when(apiClient).submitPayment(paymentRequest);

        // when
        ThrowingCallable callable =
                () ->
                        ReflectionTestUtils.invokeMethod(
                                objectUnderTest, "submitPayment", paymentRequest, paymentDate);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(TransferExecutionException.class)
                .hasMessage("An error occurred when submitting payment to the user's outbox.");
    }

    @Test
    public void shouldThrowFailedTransferExceptionWhen500ResponseAndPaymentDateIsFuture() {
        // given

        // Current date and time: 2021-11-01 07:15:37 CET
        SkandiaBankenDateUtils.setClockForTesting(
                Clock.fixed(Instant.ofEpochSecond(1635750937), ZONE_ID));

        // Payment date: 2021-11-25 00:00:00 CET
        Date paymentDate = Date.from(Instant.ofEpochSecond(1637798400));

        setUpSubmitPayment500HttpResponse();
        PaymentRequest paymentRequest = mock(PaymentRequest.class);
        doThrow(httpResponseException).when(apiClient).submitPayment(paymentRequest);

        // when
        ThrowingCallable callable =
                () ->
                        ReflectionTestUtils.invokeMethod(
                                objectUnderTest, "submitPayment", paymentRequest, paymentDate);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(TransferExecutionException.class)
                .hasMessage("An error occurred when submitting payment to the user's outbox.");
    }

    private Transfer getValidTransfer() {
        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier("91599999999"));
        transfer.setDestination(new BankGiroIdentifier("9999999"));
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.0));
        transfer.setType(TransferType.PAYMENT);

        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("Reference to recipient");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        transfer.setRemittanceInformation(remittanceInformation);

        return transfer;
    }

    private Transfer getA2ATransfer() {
        Transfer transfer = new Transfer();
        transfer.setDestination(new SwedishIdentifier("91599999999"));
        return transfer;
    }

    private Transfer getBgTransfer() {
        Transfer transfer = new Transfer();
        transfer.setDestination(new BankGiroIdentifier("9999999"));
        return transfer;
    }

    private Transfer getPgTransfer() {
        Transfer transfer = new Transfer();
        transfer.setDestination(new PlusGiroIdentifier("9999999"));
        return transfer;
    }

    private Transfer getLessThan1SekTransfer() {
        Transfer transfer = new Transfer();
        transfer.setAmount(ExactCurrencyAmount.inSEK(0.01));
        return transfer;
    }

    private Transfer get1SekTransfer() {
        Transfer transfer = new Transfer();
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.0));
        return transfer;
    }

    private Transfer getGreaterThan1SekTransfer() {
        Transfer transfer = new Transfer();
        transfer.setAmount(ExactCurrencyAmount.inSEK(100.12));
        return transfer;
    }

    private Transfer getTransferWithUnstructuredRemittanceInformation(String reference) {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue(reference);
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        Transfer transfer = new Transfer();
        transfer.setRemittanceInformation(remittanceInformation);

        return transfer;
    }

    private Transfer getTransferWithSource(String accountNumber) {
        Transfer transfer = new Transfer();
        transfer.setSource(new SwedishIdentifier(accountNumber));
        return transfer;
    }

    private PaymentSourceAccountsResponse getPaymentSourceAccountsResponse() {
        return SerializationUtils.deserializeFromString(
                "[\n"
                        + "  {\n"
                        + "    \"BankAccountName\": \"\",\n"
                        + "    \"BankAccountNumber\": \"91599999999\",\n"
                        + "    \"EncryptedBankAccountNumber\": \"dummyEncryptedBankAccountNumber1\",\n"
                        + "    \"BankAccountDisplayName\": \"Allt i Ett-konto\",\n"
                        + "    \"BankAccountDisplayNumber\": \"9159-999.999-9\",\n"
                        + "    \"DisposableAmount\": 8.0,\n"
                        + "    \"Amount\": 8.0,\n"
                        + "    \"TransferableAccountType\": 1,\n"
                        + "    \"TransferableAccountTypeName\": \"BankAccount\",\n"
                        + "    \"BankAccountTypeDisplayName\": \"Allt i Ett-konto\",\n"
                        + "    \"BankAccountHolderFirstname\": \"Firstname\",\n"
                        + "    \"BankAccountHolderSurname\": \"Lastname\",\n"
                        + "    \"Position\": 1,\n"
                        + "    \"BankAccountType\": 1\n"
                        + "  },\n"
                        + "  {\n"
                        + "    \"BankAccountName\": \"\",\n"
                        + "    \"BankAccountNumber\": \"91591111111\",\n"
                        + "    \"EncryptedBankAccountNumber\": \"dummyEncryptedBankAccountNumber2\",\n"
                        + "    \"BankAccountDisplayName\": \"Allt i Ett-konto\",\n"
                        + "    \"BankAccountDisplayNumber\": \"9159-111.111-1\",\n"
                        + "    \"DisposableAmount\": 100.0,\n"
                        + "    \"Amount\": 100.0,\n"
                        + "    \"TransferableAccountType\": 1,\n"
                        + "    \"TransferableAccountTypeName\": \"BankAccount\",\n"
                        + "    \"BankAccountTypeDisplayName\": \"Allt i Ett-konto\",\n"
                        + "    \"BankAccountHolderFirstname\": \"Firstname\",\n"
                        + "    \"BankAccountHolderSurname\": \"Lastname\",\n"
                        + "    \"Position\": 1,\n"
                        + "    \"BankAccountType\": 1\n"
                        + "  }\n"
                        + "]",
                PaymentSourceAccountsResponse.class);
    }

    private ErrorResponse getBankIdTimeout() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"StatusCode\": 500,\n"
                        + "    \"StatusMessage\": \"InternalServerError\",\n"
                        + "    \"Fields\":\n"
                        + "    [\n"
                        + "        {\n"
                        + "            \"Code\": \"0001\",\n"
                        + "            \"Field\": \"Reference\",\n"
                        + "            \"Message\": \"1c004487-2675-4ad4-9452-4c18c784f5bf\"\n"
                        + "        },\n"
                        + "        {\n"
                        + "            \"Code\": \"0002\",\n"
                        + "            \"Field\": \"Status\",\n"
                        + "            \"Message\": \"1\"\n"
                        + "        }\n"
                        + "    ],\n"
                        + "    \"ErrorCode\": \"SYPSIG0101\",\n"
                        + "    \"ErrorMessage\": \"MobiltBankIdCollectAsync: Unable to convert response in SigningProvider: Sign\"\n"
                        + "}",
                ErrorResponse.class);
    }

    private ErrorResponse getBankApiException() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"StatusCode\": 0,\n"
                        + "    \"StatusMessage\": \"0\",\n"
                        + "    \"Fields\":\n"
                        + "    [],\n"
                        + "    \"ErrorCode\": null,\n"
                        + "    \"ErrorMessage\": \"Exception of type 'Helium.Api.Common.Exceptions.HeliumApiException' was thrown.\"\n"
                        + "}",
                ErrorResponse.class);
    }

    private ErrorResponse getBankIdAlreadyInProgress() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"StatusCode\": 500,\n"
                        + "    \"StatusMessage\": \"InternalServerError\",\n"
                        + "    \"Fields\":\n"
                        + "    [],\n"
                        + "    \"ErrorCode\": \"BAPSIG0102\",\n"
                        + "    \"ErrorMessage\": \"A sign job was already active, try again.\"\n"
                        + "}",
                ErrorResponse.class);
    }

    private ErrorResponse getInvalidOcrErrorResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"StatusCode\": 400,\n"
                        + "  \"StatusMessage\": \"BadRequest\",\n"
                        + "  \"Fields\": [\n"
                        + "    {\n"
                        + "      \"Code\": null,\n"
                        + "      \"Field\": \"payments[0].OCRReference\",\n"
                        + "      \"Message\": \"OCR is not valid.\"\n"
                        + "    }\n"
                        + "  ],\n"
                        + "  \"ErrorCode\": \"HEMB0001\",\n"
                        + "  \"ErrorMessage\": \"Input validation failed.\"\n"
                        + "}",
                ErrorResponse.class);
    }

    private ErrorResponse getSubmitPaymentGenericErrorResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"StatusCode\": 500,\n"
                        + "  \"StatusMessage\": \"InternalServerError\",\n"
                        + "  \"Fields\": null,\n"
                        + "  \"ErrorCode\": \"BAPPAY0107\",\n"
                        + "  \"ErrorMessage\": \"We're experiencing technical difficulties at the moment. Please try again or contact Customer services.\"\n"
                        + "}",
                ErrorResponse.class);
    }

    private void setUpSubmitPayment500HttpResponse() {
        when(httpResponse.getStatus()).thenReturn(500);
        when(httpResponse.getType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        when(httpResponse.getBody(ErrorResponse.class))
                .thenReturn(getSubmitPaymentGenericErrorResponse());
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
    }
}
