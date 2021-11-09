package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class CreditAgricoleResponseStatusHandlerTest {

    private CreditAgricoleResponseStatusHandler statusHandler;

    @Before
    public void setUp() {
        statusHandler = new CreditAgricoleResponseStatusHandler();
    }

    @Test(expected = BankServiceException.class)
    public void shouldThrowBankSideError() {
        // given
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(500);
        when(httpResponse.getBody(String.class)).thenReturn(getInternalServerError());

        // when
        statusHandler.handleResponse(null, httpResponse);
    }

    public String getInternalServerError() {
        return "{\n"
                + "  \"timestamp\": \"2021-11-08T23:58:18.24+0100\",\n"
                + "  \"status\": 500,\n"
                + "  \"error\": \"Internal Server Error\",\n"
                + "  \"message\": \"Internal server error\",\n"
                + "  \"path\": \"/accounts/id/transactions\"\n"
                + "}";
    }
}
