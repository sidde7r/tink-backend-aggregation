package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class BpceResponseHandlerTest {

    private final BpceResponseHandler responseHandler = new BpceResponseHandler();

    private HttpRequest httpRequest;
    private HttpResponse httpResponse;
    private MultivaluedMapImpl headers;

    @Before
    public void setUp() {
        httpRequest = Mockito.mock(HttpRequest.class);
        httpResponse = Mockito.mock(HttpResponse.class);
        headers = new MultivaluedMapImpl();

        Mockito.when(httpResponse.getHeaders()).thenReturn(headers);
    }

    @Test
    public void shouldNotThrowAnyException() {
        // given
        Mockito.when(httpResponse.getStatus()).thenReturn(200);

        // then
        Assertions.assertThatNoException()
                .isThrownBy(() -> responseHandler.handleResponse(httpRequest, httpResponse));
    }

    @Test
    public void shouldThrowBankServiceErrorIfHtmlResponse() {
        // given
        headers.putSingle("Page_Erreur", "");
        headers.putSingle("Content-Type", "application/octet-stream");
        Mockito.when(httpResponse.getStatus()).thenReturn(200);

        // then
        Assertions.assertThatExceptionOfType(BankServiceException.class)
                .isThrownBy(() -> responseHandler.handleResponse(httpRequest, httpResponse));
    }
}
