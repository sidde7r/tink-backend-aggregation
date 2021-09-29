package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(JUnitParamsRunner.class)
public class RedsysHttpResponseStatusHandlerTest {

    private HttpResponse httpResponse;
    private HttpRequest httpRequest;
    private RedsysHttpResponseStatusHandler objectUnderTest;

    @Before
    public void init() {
        httpResponse = Mockito.mock(HttpResponse.class);
        httpRequest = Mockito.mock(HttpRequest.class);
        objectUnderTest = new RedsysHttpResponseStatusHandler();
    }

    @Test
    @Parameters({"{\"tppMessages\":[{\"category\":\"ERROR\",\"code\":\"Internal Server Error\"}]}"})
    public void shouldThrowBankTemporaryUnavailable(String body) {
        // given:
        Mockito.when(httpResponse.getStatus()).thenReturn(500);
        Mockito.when(httpResponse.getBody(String.class)).thenReturn(body);

        // when
        Throwable throwable =
                Assertions.catchThrowable(
                        () -> objectUnderTest.handleResponse(httpRequest, httpResponse));

        // then
        Assertions.assertThat(throwable).isInstanceOf(BankServiceException.class);
        BankServiceException bankServiceException = (BankServiceException) throwable;
        Assertions.assertThat(bankServiceException.getError())
                .isEqualTo(BankServiceError.BANK_SIDE_FAILURE);
    }

    @Test(expected = HttpResponseException.class)
    public void shouldThrowHttpResponseExceptionWhenStatus500WithNoBody() {
        // given:
        Mockito.when(httpResponse.getStatus()).thenReturn(500);
        Mockito.when(httpResponse.getBody(String.class)).thenReturn("");

        // when
        objectUnderTest.handleResponse(httpRequest, httpResponse);
    }
}
