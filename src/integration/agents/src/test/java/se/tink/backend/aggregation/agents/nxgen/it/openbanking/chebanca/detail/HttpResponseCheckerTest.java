package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.exception.UnsuccessfulApiCallException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class HttpResponseCheckerTest {
    private static final int ERROR_RESPONSE_CODE = 300;
    private static final int SUCCESSFUL_RESPONSE_CODE = 200;

    @Test
    public void shouldNotThrowIfSuccessfulResponse() {
        Throwable thrown =
                catchThrowable(
                        () ->
                                HttpResponseChecker.checkIfSuccessfulResponse(
                                        getResponse(SUCCESSFUL_RESPONSE_CODE),
                                        SUCCESSFUL_RESPONSE_CODE,
                                        ""));

        assertNull(thrown);
    }

    @Test
    public void shouldThrowIfUnsuccessfulResponse() {
        Throwable thrown =
                catchThrowable(
                        () ->
                                HttpResponseChecker.checkIfSuccessfulResponse(
                                        getResponse(ERROR_RESPONSE_CODE),
                                        SUCCESSFUL_RESPONSE_CODE,
                                        "Some message."));

        Assertions.assertThat(thrown)
                .isInstanceOf(UnsuccessfulApiCallException.class)
                .hasMessage("Some message. Error response code: " + ERROR_RESPONSE_CODE);
    }

    private HttpResponse getResponse(int code) {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(code);
        return response;
    }
}
