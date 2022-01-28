package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.errorhandler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.payment.DuplicatePaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.errors.SparkassenErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.errors.SparkassenErrorHandler.ErrorSource;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class SparkassenErrorHandlerTest {

    private static final String RESOURCE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/sparkassen/resources/";

    @Test
    @Parameters(method = "exceptionsToTest")
    public <T extends RuntimeException> void areHttpExceptionsCorrectlyMapped(
            String exceptionMessage,
            String errorMessage,
            ErrorSource errorSource,
            Class<T> exceptionClass) {
        testExceptionMapping(exceptionMessage, errorMessage, errorSource, exceptionClass);
    }

    private <T> void testExceptionMapping(
            String exceptionMessage,
            String errorMessage,
            ErrorSource errorSource,
            Class<T> exceptionClass) {

        // when
        HttpResponseException exception = getResponseExceptionWithMessage(exceptionMessage);

        // then
        assertThatThrownBy(() -> SparkassenErrorHandler.handeHttpException(exception, errorSource))
                .isInstanceOf(exceptionClass)
                .hasMessage(errorMessage);
    }

    @SuppressWarnings("unused")
    public Object[] exceptionsToTest() {
        return new Object[] {
            new Object[] {
                "9010- Der Auftrag wurde nicht ausgeführt. - 9390- Auftrag wegen Doppeleinreichung abgelehnt. - 3900- Ggf neuen Auftrag mit ge?ndertem Verwendungszweck einreichen",
                "The payment could not be made because an identical payment is already registered",
                ErrorSource.CREATE_PAYMENT,
                DuplicatePaymentException.class
            },
            new Object[] {
                "9010- Der Auftrag wurde nicht ausgeführt. - 9390- Auftrag wegen Doppeleinreichung abgelehnt. - 3900- Ggf neuen Auftrag mit ge?ndertem Verwendungszweck einreichen",
                "The payment could not be made because an identical payment is already registered",
                ErrorSource.FETCH_PAYMENT,
                DuplicatePaymentException.class
            },
            new Object[] {
                "9010- Der Auftrag wurde nicht ausgeführt. - 9010- Systemfehler - bitte wenden Sie sich an Ihren Kundenberater",
                "The payment was rejected by the bank.",
                ErrorSource.CREATE_PAYMENT,
                PaymentRejectedException.class
            },
            new Object[] {
                "9010- Der Auftrag wurde nicht ausgeführt. - 9010- Systemfehler - bitte wenden Sie sich an Ihren Kundenberater",
                "The payment was rejected by the bank.",
                ErrorSource.FETCH_PAYMENT,
                PaymentRejectedException.class
            },
            new Object[] {
                "9941- Die eingegebene TAN ist falsch. - 3933- Bitte benutzen Sie die Karte",
                "Cause: LoginError.INCORRECT_CHALLENGE_RESPONSE",
                ErrorSource.CREATE_PAYMENT,
                LoginException.class
            },
            new Object[] {
                "9941- Die eingegebene TAN ist falsch. - 3933- Bitte benutzen Sie die Karte",
                "Cause: LoginError.INCORRECT_CHALLENGE_RESPONSE",
                ErrorSource.FETCH_PAYMENT,
                LoginException.class
            },
            new Object[] {
                "9010- Der Auftrag wurde nicht ausgeführt. - 9370- Es ist keine Auftragsberechtigung vorhanden",
                "Payment was not authorised. Please try again.",
                ErrorSource.FETCH_PAYMENT,
                PaymentAuthorizationException.class
            },
            new Object[] {
                "9010- Der Auftrag wurde nicht ausgeführt. - 9370- Es ist keine Auftragsberechtigung vorhanden",
                "Payment was not authorised. Please try again.",
                ErrorSource.CREATE_PAYMENT,
                PaymentAuthorizationException.class
            },
            new Object[] {
                "9370- Es ist keine Auftragsberechtigung vorhanden.",
                "Payment was not authorised. Please try again.",
                ErrorSource.FETCH_PAYMENT,
                PaymentAuthorizationException.class
            },
            new Object[] {
                "9370- Es ist keine Auftragsberechtigung vorhanden.",
                "Payment was not authorised. Please try again.",
                ErrorSource.CREATE_PAYMENT,
                PaymentAuthorizationException.class
            },
            new Object[] {
                "9010- Auftrag führt zu Überschreitung des vereinbarten ZV-Tageslimits",
                "The number of transactions exceeds the acceptance limit.",
                ErrorSource.FETCH_PAYMENT,
                PaymentRejectedException.class
            },
            new Object[] {
                "9010- Auftrag führt zu Überschreitung des vereinbarten ZV-Tageslimits",
                "The number of transactions exceeds the acceptance limit.",
                ErrorSource.CREATE_PAYMENT,
                PaymentRejectedException.class
            }
        };
    }

    private HttpResponseException getResponseExceptionWithMessage(String message) {
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getBody(ErrorResponse.class)).thenReturn(getErrorResponse(message));

        HttpResponseException exception = mock(HttpResponseException.class);
        when(exception.getResponse()).thenReturn(httpResponse);
        when(exception.getResponse().hasBody()).thenReturn(true);

        return exception;
    }

    private ErrorResponse getErrorResponse(String message) {
        return SerializationUtils.deserializeFromString(
                getResponseBody(message), ErrorResponse.class);
    }

    @SneakyThrows
    private String getResponseBody(String message) {
        String responseBody =
                FileUtils.readFileToString(
                        Paths.get(RESOURCE_PATH, "http_response_exception.json").toFile(),
                        StandardCharsets.UTF_8);
        return responseBody.replace("PSU_MESSAGE", message);
    }
}
