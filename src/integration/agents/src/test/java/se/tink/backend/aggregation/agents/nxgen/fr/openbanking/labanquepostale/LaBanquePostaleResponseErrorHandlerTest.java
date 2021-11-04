package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LaBanquePostaleResponseErrorHandlerTest {

    private LaBanquePostaleResponseErrorHandler errorHandler;

    @Before
    public void setup() {
        errorHandler = new LaBanquePostaleResponseErrorHandler();
    }

    @Test(expected = BankServiceException.class)
    public void shouldThrowBankSideFailureException() {
        // given
        HttpResponse response = Mockito.mock(HttpResponse.class);
        LaBanquePostaleErrorResponse errorResponse =
                Mockito.mock(LaBanquePostaleErrorResponse.class);

        given(response.getStatus()).willReturn(500);
        given(errorResponse.isBankSideError()).willReturn(true);
        given(response.getBody(LaBanquePostaleErrorResponse.class)).willReturn(errorResponse);

        // when
        errorHandler.handleResponse(null, response);
    }

    @Test(expected = BankServiceException.class)
    public void shouldThrowBankSideFailureExceptionForProxyError() {
        // given
        HttpResponse response = Mockito.mock(HttpResponse.class);
        LaBanquePostaleProxyErrorResponse proxyErrorResponse = getProxyError();

        given(response.getStatus()).willReturn(502);
        given(response.getBody(LaBanquePostaleProxyErrorResponse.class))
                .willReturn(proxyErrorResponse);

        // when
        errorHandler.handleResponse(null, response);
    }

    private LaBanquePostaleProxyErrorResponse getProxyError() {
        return SerializationUtils.deserializeFromString(
                "{\"proxyError\":true,\"errorText\":\"Connection reset\"}",
                LaBanquePostaleProxyErrorResponse.class);
    }
}
