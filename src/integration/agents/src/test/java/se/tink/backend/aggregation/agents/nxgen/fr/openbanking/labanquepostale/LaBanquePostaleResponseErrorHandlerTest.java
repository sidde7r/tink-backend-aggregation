package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

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

        given(errorResponse.isBankSideError()).willReturn(true);
        given(response.getBody(LaBanquePostaleErrorResponse.class)).willReturn(errorResponse);

        // when
        errorHandler.handleResponse(null, response);
    }
}
