package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.component.detail;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail.HttpResponseChecker;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;

public class HttpResponseCheckerTest {

    @Test(expected = Test.None.class)
    public void shouldNotThrowIfSuccessfulResponse() {
        HttpResponseChecker.checkIfSuccessfulResponse(getResponse(200), 200, "");
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfUnsuccessfulResponse() {
        HttpResponseChecker.checkIfSuccessfulResponse(getResponse(300), 200, "");
    }

    private HttpResponse getResponse(int code) {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(code);
        return response;
    }
}
