package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Builder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.ArgentaErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RunWith(JUnitParamsRunner.class)
public class ArgentaKnownErrorsFilterTest {

    private final ArgentaKnownErrorsFilter objectUnderTest = new ArgentaKnownErrorsFilter();
    private final Filter filter = mock(Filter.class);
    private final HttpRequest request = mock(HttpRequest.class);
    private final HttpResponse response = mock(HttpResponse.class);

    @Before
    public void init() {
        objectUnderTest.setNext(filter);
        when(filter.handle(request)).thenReturn(response);
    }

    @Test
    @Parameters(method = "errorCodeExceptionsParameters")
    public void shouldThrowProperExceptionForErrorCode(
            String errorCode, int statusCode, RuntimeException expected) {
        // given
        ArgentaErrorResponse argentaErrorResponse = mock(ArgentaErrorResponse.class);
        when(argentaErrorResponse.getCode()).thenReturn(errorCode);

        // and
        when(response.getStatus()).thenReturn(statusCode);
        when(response.hasBody()).thenReturn(true);
        when(response.getBody(ArgentaErrorResponse.class)).thenReturn(argentaErrorResponse);

        // when
        Throwable throwable = catchThrowable(() -> objectUnderTest.handle(request));

        // then
        assertThat(throwable).usingRecursiveComparison().isEqualTo(expected);
    }

    private Object[] errorCodeExceptionsParameters() {
        return new Object[] {
            ErrorsParameters.builder()
                    .errorCode("error.authentication.failed")
                    .statusCode(400)
                    .expectedException(LoginError.INCORRECT_CREDENTIALS.exception())
                    .build()
                    .toParametersWithCodeError(),
            ErrorsParameters.builder()
                    .errorCode("error.invalid.request")
                    .statusCode(500)
                    .expectedException(BankServiceError.BANK_SIDE_FAILURE.exception())
                    .build()
                    .toParametersWithCodeError(),
        };
    }

    @Test
    @Parameters(method = "errorMessageExceptionsParameters")
    public void shouldThrowProperExceptionForErrorMessage(
            String errorMessage, int statusCode, RuntimeException expected) {
        // given
        ArgentaErrorResponse argentaErrorResponse = mock(ArgentaErrorResponse.class);
        when(argentaErrorResponse.getCode()).thenReturn("error.sbb.testo");
        when(argentaErrorResponse.getMessage()).thenReturn(errorMessage);

        // and
        when(response.getStatus()).thenReturn(statusCode);
        when(response.hasBody()).thenReturn(true);
        when(response.getBody(ArgentaErrorResponse.class)).thenReturn(argentaErrorResponse);

        // when
        Throwable throwable = catchThrowable(() -> objectUnderTest.handle(request));

        // then
        assertThat(throwable).usingRecursiveComparison().isEqualTo(expected);
    }

    private Object[] errorMessageExceptionsParameters() {
        return new Object[] {
            ErrorsParameters.builder()
                    .errorMessage("Test maximumaantal actieve registraties test")
                    .statusCode(400)
                    .expectedException(LoginError.REGISTER_DEVICE_ERROR.exception())
                    .build()
                    .toParametersWithErrorMessage(),
            ErrorsParameters.builder()
                    .errorMessage("Wrong credentials man de logingegevens zijn niet juist test")
                    .statusCode(401)
                    .expectedException(LoginError.INCORRECT_CREDENTIALS.exception())
                    .build()
                    .toParametersWithErrorMessage(),
            ErrorsParameters.builder()
                    .errorMessage("Test je hebt te vaak een foute pincode ingevoerd test")
                    .statusCode(403)
                    .expectedException(LoginError.INCORRECT_CHALLENGE_RESPONSE.exception())
                    .build()
                    .toParametersWithErrorMessage(),
            ErrorsParameters.builder()
                    .errorMessage("Account is geblokkeerd test")
                    .statusCode(403)
                    .expectedException(AuthorizationError.ACCOUNT_BLOCKED.exception())
                    .build()
                    .toParametersWithErrorMessage(),
            ErrorsParameters.builder()
                    .errorMessage("We lossen het probleem zo snel mogelijk op test")
                    .statusCode(503)
                    .expectedException(BankServiceError.BANK_SIDE_FAILURE.exception())
                    .build()
                    .toParametersWithErrorMessage(),
            ErrorsParameters.builder()
                    .errorMessage("Oh no, er ging iets mis test")
                    .statusCode(500)
                    .expectedException(BankServiceError.BANK_SIDE_FAILURE.exception())
                    .build()
                    .toParametersWithErrorMessage(),
        };
    }

    @Test
    @Parameters(method = "unhandledResponseParameters")
    public void shouldReturnHttpResponse(String errorMessage, String errorCode) {
        // given
        ArgentaErrorResponse argentaErrorResponse = mock(ArgentaErrorResponse.class);
        when(argentaErrorResponse.getCode()).thenReturn(errorCode);
        when(argentaErrorResponse.getMessage()).thenReturn(errorMessage);

        // and
        when(response.getStatus()).thenReturn(400);
        when(response.getBody(ArgentaErrorResponse.class)).thenReturn(argentaErrorResponse);

        // when
        HttpResponse result = objectUnderTest.handle(request);

        // then
        assertThat(result).usingRecursiveComparison().isEqualTo(response);
    }

    private Object[] unhandledResponseParameters() {
        return new Object[] {
            ErrorsParameters.builder()
                    .errorMessage("maximumaantal actieve registraties")
                    .errorCode("error.test")
                    .build()
                    .toParametersWithoutException(),
            ErrorsParameters.builder()
                    .errorMessage("Some test message")
                    .errorCode("error.sbb")
                    .build()
                    .toParametersWithoutException(),
            ErrorsParameters.builder()
                    .errorMessage("Step up is nodig.")
                    .errorCode("error.signing.stepup.required")
                    .build()
                    .toParametersWithoutException(),
        };
    }

    @Test
    public void shouldReturnHttpResponseWhenBodyCannotBeParsedToArgentaErrorResponse() {
        // given
        when(response.getStatus()).thenReturn(400);
        when(response.getBody(ArgentaErrorResponse.class))
                .thenThrow(new HttpClientException(request));

        // when
        HttpResponse result = objectUnderTest.handle(request);

        // then
        assertThat(result).usingRecursiveComparison().isEqualTo(response);
    }

    @Builder
    private static class ErrorsParameters {
        private final String errorCode;
        private final String errorMessage;
        private final int statusCode;
        private final RuntimeException expectedException;

        private Object[] toParametersWithCodeError() {
            return new Object[] {this.errorCode, this.statusCode, this.expectedException};
        }

        private Object[] toParametersWithErrorMessage() {
            return new Object[] {this.errorMessage, this.statusCode, this.expectedException};
        }

        private Object[] toParametersWithoutException() {
            return new Object[] {this.errorMessage, this.errorCode};
        }
    }
}
