package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.signableoperation.enums.InternalStatus;

public class UkOpenBankingPaymentErrorHandlerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testExceedDailyLimitFailure() throws IOException {

        String source =
                getExceptionBody(
                        "      \"Message\": \"Forbidden: This payment exceeds the daily payment limit\"\n");

        HttpResponseException httpResponseException = mock(HttpResponseException.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getBody(ErrorResponse.class))
                .thenReturn(objectMapper.readValue(source, ErrorResponse.class));

        // when
        PaymentException paymentException =
                UkOpenBankingPaymentErrorHandler.getPaymentError(httpResponseException);

        // then
        assertEquals(
                InternalStatus.TRANSFER_LIMIT_REACHED.toString(),
                paymentException.getInternalStatus());
        assertEquals(
                "Forbidden: This payment exceeds the daily payment limit",
                paymentException.getMessage());
    }

    @Test
    public void testReAuthenticationRequiredFailure() throws IOException {

        String source =
                getExceptionBody(
                        "      \"Message\": \"Not having required scope or permission to perform this action. Please contact support for further details.\"\n");

        HttpResponseException httpResponseException = mock(HttpResponseException.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getBody(ErrorResponse.class))
                .thenReturn(objectMapper.readValue(source, ErrorResponse.class));

        // when
        PaymentException paymentException =
                UkOpenBankingPaymentErrorHandler.getPaymentError(httpResponseException);

        // then
        assertEquals(
                InternalStatus.PAYMENT_AUTHORIZATION_FAILED.toString(),
                paymentException.getInternalStatus());
        assertEquals(
                "Your payment request could not be authorized by the bank at the moment. Please try executing payment again.",
                paymentException.getMessage());
    }

    @Test
    public void testSuspiciousTransactionFailure() throws IOException {

        String source =
                getExceptionBody(
                        "      \"Message\": \"Our systems have identified your transaction as highly suspicious.\"\n");

        HttpResponseException httpResponseException = mock(HttpResponseException.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getBody(ErrorResponse.class))
                .thenReturn(objectMapper.readValue(source, ErrorResponse.class));

        // when
        PaymentException paymentException =
                UkOpenBankingPaymentErrorHandler.getPaymentError(httpResponseException);

        // then
        assertEquals(
                InternalStatus.ACCOUNT_BLOCKED_FOR_TRANSFER.toString(),
                paymentException.getInternalStatus());
        assertEquals(
                "Bank systems have identified your transaction as highly suspicious.",
                paymentException.getMessage());
    }

    @Test
    public void testSameSenderAndRecipient() throws IOException {

        String source =
                getExceptionBody(
                        "      \"Message\": \"Sender and recipient can not be the same user.\"\n");

        HttpResponseException httpResponseException = mock(HttpResponseException.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getBody(ErrorResponse.class))
                .thenReturn(objectMapper.readValue(source, ErrorResponse.class));

        // when
        PaymentException paymentException =
                UkOpenBankingPaymentErrorHandler.getPaymentError(httpResponseException);

        // then
        assertEquals(
                InternalStatus.INVALID_DESTINATION_ACCOUNT.toString(),
                paymentException.getInternalStatus());
        assertEquals("Sender and recipient can not be the same.", paymentException.getMessage());
    }

    @Test
    public void testDomesticPaymentsNotAvailable() throws IOException {

        String source =
                getExceptionBody(
                        "      \"Message\": \"Domestic payments in EUR are not available for PF.\"\n");

        HttpResponseException httpResponseException = mock(HttpResponseException.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getBody(ErrorResponse.class))
                .thenReturn(objectMapper.readValue(source, ErrorResponse.class));

        // when
        PaymentException paymentException =
                UkOpenBankingPaymentErrorHandler.getPaymentError(httpResponseException);

        // then
        assertEquals(
                InternalStatus.INVALID_DESTINATION_ACCOUNT.toString(),
                paymentException.getInternalStatus());
        assertEquals("Domestic payments in EUR are not available.", paymentException.getMessage());
    }

    @Test
    public void testUnknownException() throws IOException {

        String source = getExceptionBody("      \"Message\": \"Unknown error.\"\n");

        HttpResponseException httpResponseException = mock(HttpResponseException.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getBody(ErrorResponse.class))
                .thenReturn(objectMapper.readValue(source, ErrorResponse.class));

        // when
        PaymentException paymentException =
                UkOpenBankingPaymentErrorHandler.getPaymentError(httpResponseException);

        // then
        assertEquals(
                InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET.toString(),
                paymentException.getInternalStatus());
        assertEquals("Payment failed.", paymentException.getMessage());
    }

    @Test
    public void testIsProfileRestricted() throws IOException {

        String source =
                "{\n"
                        + "  \"Code\": \"9038\",\n"
                        + "  \"Id\": \"c351be0f-8c1c-472f-8a49-b609adf54076\",\n"
                        + "  \"Message\": \"Request error found.\"\n"
                        + "}";

        HttpResponseException httpResponseException = mock(HttpResponseException.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getBody(ErrorResponse.class))
                .thenReturn(objectMapper.readValue(source, ErrorResponse.class));

        // when
        PaymentException paymentException =
                UkOpenBankingPaymentErrorHandler.getPaymentError(httpResponseException);

        // then
        assertEquals(
                InternalStatus.ACCOUNT_BLOCKED_FOR_TRANSFER.toString(),
                paymentException.getInternalStatus());
        assertEquals("Profile is restricted.", paymentException.getMessage());
    }

    private String getExceptionBody(String s) {
        return "{\n"
                + "  \"Code\": \"400 BadRequest\",\n"
                + "  \"Id\": \"c351be0f-8c1c-472f-8a49-b609adf54076\",\n"
                + "  \"Message\": \"Request error found.\",\n"
                + "  \"Errors\": [\n"
                + "    {\n"
                + "      \"ErrorCode\": \"Invalid request parameters\",\n"
                + s
                + "    }\n"
                + "  ]\n"
                + "}";
    }
}
