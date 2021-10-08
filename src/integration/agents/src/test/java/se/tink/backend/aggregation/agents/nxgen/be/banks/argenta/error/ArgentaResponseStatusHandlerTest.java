package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.error;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

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
public class ArgentaResponseStatusHandlerTest {

    private static final String ERROR_SIGNING_STEPUP_REQUIRED = "error.signing.stepup.required";
    private final ArgentaResponseStatusHandler responseHandler = new ArgentaResponseStatusHandler();
    private final ArgentaErrorResponse argentaErrorResponse = mock(ArgentaErrorResponse.class);
    private final HttpResponse response = mock(HttpResponse.class);
    private final HttpRequest request = mock(HttpRequest.class);

    @Test
    public void shouldNotThrowExceptionOnSigningStepupRequiredError() {
        // given
        given(argentaErrorResponse.getCode()).willReturn(ERROR_SIGNING_STEPUP_REQUIRED);

        // and
        given(response.getStatus()).willReturn(400);
        given(response.hasBody()).willReturn(true);
        given(response.getBody(ArgentaErrorResponse.class)).willReturn(argentaErrorResponse);

        // expect
        assertThatNoException().isThrownBy(() -> responseHandler.handleResponse(request, response));
    }

    @Test
    @Parameters(method = "errorsParameters")
    public void shouldThrowExceptionOnErrorDifferentThanSigningStepupRequired(
            int statusCode, String code, boolean hasBody) {
        // given
        given(argentaErrorResponse.getCode()).willReturn(code);

        // and
        given(response.getStatus()).willReturn(statusCode);
        given(response.hasBody()).willReturn(hasBody);
        given(response.getBody(ArgentaErrorResponse.class)).willReturn(argentaErrorResponse);

        // expect
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
        given(argentaErrorResponse.getCode()).willReturn(ERROR_SIGNING_STEPUP_REQUIRED);

        // and
        given(response.getStatus()).willReturn(400);
        given(response.hasBody()).willReturn(true);
        given(response.getBody(ArgentaErrorResponse.class))
                .willThrow(
                        new HttpClientException(
                                "Oh no, cannot map the test response. Anyway...", null));

        // expect
        assertThatThrownBy(() -> responseHandler.handleResponse(request, response))
                .isInstanceOf(HttpResponseException.class);
    }
}
