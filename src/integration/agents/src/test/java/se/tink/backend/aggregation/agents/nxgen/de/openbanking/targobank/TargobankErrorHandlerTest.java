package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagErrorHandler.ErrorSource;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(JUnitParamsRunner.class)
public class TargobankErrorHandlerTest {

    private final TargobankErrorHandler errorHandler = new TargobankErrorHandler();

    @Test
    @Parameters(method = "parametersForHandleErrorShouldThrowExpectedRuntimeException")
    public void handleErrorShouldThrowExpectedRuntimeException(
            String fileSource, ErrorSource errorSource, RuntimeException runtimeException) {
        // given
        HttpResponseException httpResponseException = mockHttpResponseException(fileSource);
        // when
        Throwable thrown =
                catchThrowable(() -> errorHandler.handleError(httpResponseException, errorSource));
        // then
        assertThat(thrown)
                .isInstanceOf(runtimeException.getClass())
                .hasMessageContaining(runtimeException.getMessage());
    }

    @SuppressWarnings("unused")
    private Object[] parametersForHandleErrorShouldThrowExpectedRuntimeException() {
        return new Object[] {
            new Object[] {
                TestDataReader.INCORRECT_CREDENTIALS,
                ErrorSource.AUTHORISATION_USERNAME_PASSWORD,
                LoginError.INCORRECT_CREDENTIALS.exception()
            },
            new Object[] {
                TestDataReader.FIRST_PAYMENT_ONLINE_BANKING,
                ErrorSource.AUTHORISATION_USERNAME_PASSWORD,
                new PaymentRejectedException(
                        "Payment rejected, you must make the first instant transfer in online banking.")
            },
            new Object[] {
                TestDataReader.CONTACT_WITH_BANK,
                ErrorSource.AUTHORISATION_USERNAME_PASSWORD,
                new PaymentRejectedException("Payment rejected, please contact with bank.")
            },
            new Object[] {
                TestDataReader.INCORRECT_CHALLENGE_RESPONSE,
                ErrorSource.OTP_STEP,
                LoginError.INCORRECT_CHALLENGE_RESPONSE.exception()
            },
            new Object[] {
                TestDataReader.FIRST_PAYMENT_ONLINE_BANKING,
                ErrorSource.SELECT_AUTHORIZATION_METHOD,
                new PaymentRejectedException(
                        "Payment rejected, you must make the first instant transfer in online banking.")
            },
            new Object[] {
                TestDataReader.CONTACT_WITH_BANK,
                ErrorSource.SELECT_AUTHORIZATION_METHOD,
                new PaymentRejectedException("Payment rejected, please contact with bank.")
            },
            new Object[] {
                TestDataReader.RECIPIENT_BANK_NOT_SUPPORTING_INSTANT_PAYMENTS,
                ErrorSource.GET_AUTHORIZATION_STATUS,
                new PaymentRejectedException(
                        "Payment rejected, recipient bank doesn't support instant payments.")
            }
        };
    }

    private HttpResponseException mockHttpResponseException(String fileSource) {
        HttpResponse response = mock(HttpResponse.class);
        HttpResponseException exception = new HttpResponseException(null, response);
        ErrorResponse errorResponse = TestDataReader.readFromFile(fileSource, ErrorResponse.class);
        when(exception.getResponse().hasBody()).thenReturn(true);
        when(exception.getResponse().getBody(ErrorResponse.class)).thenReturn(errorResponse);
        return exception;
    }
}
