package se.tink.backend.aggregation.agents.nxgen.de.openbanking.degussabank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagErrorHandler.ErrorSource;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(JUnitParamsRunner.class)
public class DegussabankErrorHandlerTest {

    private final DegussabankErrorHandler errorHandler = new DegussabankErrorHandler();

    @Test
    @Parameters
    public void handleErrorShouldThrowExpectedError(
            String fileSource, ErrorSource errorSource, RuntimeException runtimeException) {
        // given
        HttpResponseException httpResponseException = mockHttpResponseException(fileSource);
        // when
        Throwable thrown =
                catchThrowable(() -> errorHandler.handleError(httpResponseException, errorSource));
        // then
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(runtimeException.getMessage());
    }

    private Object[] parametersForHandleErrorShouldThrowExpectedError() {
        return new Object[] {
            new Object[] {
                TestDataReader.INCORRECT_CREDENTIALS,
                ErrorSource.AUTHORISATION_USERNAME_PASSWORD,
                LoginError.INCORRECT_CREDENTIALS.exception()
            },
            new Object[] {
                TestDataReader.INCORRECT_CHALLENGE_RESPONSE,
                ErrorSource.OTP_STEP,
                LoginError.INCORRECT_CHALLENGE_RESPONSE.exception()
            },
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
