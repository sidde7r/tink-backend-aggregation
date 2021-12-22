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
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(JUnitParamsRunner.class)
public class RedsysHttpResponseStatusHandlerTest {

    private HttpResponse httpResponse;
    private HttpRequest httpRequest;
    private RedsysHttpResponseStatusHandler objectUnderTest;
    private PersistentStorage persistentStorage;

    @Before
    public void init() {
        httpResponse = Mockito.mock(HttpResponse.class);
        httpRequest = Mockito.mock(HttpRequest.class);
        persistentStorage = Mockito.mock(PersistentStorage.class);
        objectUnderTest = new RedsysHttpResponseStatusHandler(persistentStorage);
    }

    @Test
    @Parameters(method = "bankSiteErrorResponses")
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
        Mockito.verify(persistentStorage, Mockito.never()).clear();
        Assertions.assertThat(bankServiceException.getError())
                .isEqualTo(BankServiceError.BANK_SIDE_FAILURE);
    }

    private Object[] bankSiteErrorResponses() {
        return new Object[] {
            "{\"tppMessages\":[{\"category\":\"ERROR\"\\,\"code\":\"Internal Server Error\"}]}",
            "{\"tppMessages\":[{\"category\":\"ERROR\"\\,\"code\":\"INTERNAL_SERVER_ERROR\"\\,\"text\":\"An unexpected error has occurred\"}]}"
        };
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
