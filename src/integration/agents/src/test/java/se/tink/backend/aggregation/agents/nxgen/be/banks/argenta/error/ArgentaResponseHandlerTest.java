package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.error;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.ArgentaErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(JUnitParamsRunner.class)
public class ArgentaResponseHandlerTest {

    private static final String ERROR_SIGNING_STEPUP_REQUIRED = "error.signing.stepup.required";
    private final ArgentaResponseHandler responseHandler = new ArgentaResponseHandler();
    private final HttpResponse response = mock(HttpResponse.class);
    private final HttpRequest request = mock(HttpRequest.class);

    @Test
    public void shouldNotThrowExceptionOnSigningStepupRequiredError() {
        // given
        ArgentaErrorResponse argentaErrorResponse = mock(ArgentaErrorResponse.class);
        when(argentaErrorResponse.getCode()).thenReturn(ERROR_SIGNING_STEPUP_REQUIRED);

        // and
        when(response.getStatus()).thenReturn(400);
        when(response.hasBody()).thenReturn(true);
        when(response.getBody(ArgentaErrorResponse.class)).thenReturn(argentaErrorResponse);

        // when & then
        assertThatNoException().isThrownBy(() -> responseHandler.handleResponse(request, response));
    }

    @Test
    @Parameters(method = "errorsParameters")
    public void shouldThrowExceptionOnErrorDifferentThanSigningStepupRequired(
            int statusCode, String code, boolean hasBody) {
        // given
        ArgentaErrorResponse argentaErrorResponse = mock(ArgentaErrorResponse.class);
        when(argentaErrorResponse.getCode()).thenReturn(code);

        // and
        when(response.getStatus()).thenReturn(statusCode);
        when(response.hasBody()).thenReturn(hasBody);
        when(response.getBody(ArgentaErrorResponse.class)).thenReturn(argentaErrorResponse);

        // when & then
        assertThatThrownBy(() -> responseHandler.handleResponse(request, response))
                .isInstanceOf(HttpResponseException.class);
    }

    private Object[] errorsParameters() {
        return new Object[] {
            new Object[] {401, ERROR_SIGNING_STEPUP_REQUIRED, true},
            new Object[] {400, "error.something.else", true},
            new Object[] {400, ERROR_SIGNING_STEPUP_REQUIRED, false},
        };
    }

    @Test
    public void shouldThrowExceptionWhenResponseBodyCannotBeMappedToArgentaErrorResponse() {
        // given
        ArgentaErrorResponse argentaErrorResponse = mock(ArgentaErrorResponse.class);
        when(argentaErrorResponse.getCode()).thenReturn(ERROR_SIGNING_STEPUP_REQUIRED);

        // and
        when(response.getStatus()).thenReturn(400);
        when(response.hasBody()).thenReturn(true);
        when(response.getBody(ArgentaErrorResponse.class))
                .thenThrow(
                        new HttpClientException(
                                "Oh no, cannot map the test response. Anyway...", null));

        // when & then
        assertThatThrownBy(() -> responseHandler.handleResponse(request, response))
                .isInstanceOf(HttpResponseException.class);
    }
}
