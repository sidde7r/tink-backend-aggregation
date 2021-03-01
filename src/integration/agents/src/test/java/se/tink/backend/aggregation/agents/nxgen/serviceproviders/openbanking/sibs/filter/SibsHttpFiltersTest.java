package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.filter;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class SibsHttpFiltersTest {

    private HttpResponse response;
    private StubFilter stubFilter = new StubFilter();
    private HttpRequest request = Mockito.mock(HttpRequest.class);

    private static final String CONSENT_INVALID_SIBS_MESSAGE =
            "{\"transactionStatus\":\"RJCT\",\"tppMessages\":[{\"category\":\"ERROR\",\"code\":\"CONSENT_INVALID\",\"text\":\"The\n"
                    + " * consent definition is not complete or invalid. In case of being not complete, the bank is not\n"
                    + " * supporting a completion of the consent towards the PSU. Additional information will be\n"
                    + " * provided.\"}]}";

    private static final String SERVICE_INVALID_SIBS_MESSAGE =
            "{\"transactionStatus\":\"RJCT\",\"tppMessages\":[{\"category\":\"ERROR\",\"code\":\"SERVICE_INVALID\",\"text\":\n"
                    + " * \"The addressed service is not valid for the addressed resources.\"}]}";

    private static final String RATE_LIMIT_SIBS_MESSAGE =
            "{ \"httpCode\":\"429\", \"httpMessage\":\"Too Many Requests\", \"moreInformation\":\"Rate Limit exceeded\"\n"
                    + " * }";

    @Rule public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        response = Mockito.mock(HttpResponse.class);
        stubFilter.setResponse(response);
    }

    @Test
    public void shouldThrowBankServiceExceptionWhenSibsReturns401ConsentInvalid() {
        Filter filter = new ConsentInvalidErrorFilter();
        filter.setNext(stubFilter);

        int httpCode = 401;

        when(response.getStatus()).thenReturn(httpCode);
        when(response.getBody(String.class)).thenReturn(CONSENT_INVALID_SIBS_MESSAGE);

        thrown.expect(SessionException.class);
        thrown.expectMessage(
                "Http status: " + httpCode + " Error body: " + CONSENT_INVALID_SIBS_MESSAGE);

        filter.handle(request);
    }

    @Test
    public void shouldPassIfErrorIsNot401ConsentInvalid() {
        Filter filter = new ConsentInvalidErrorFilter();
        filter.setNext(stubFilter);

        when(response.getStatus()).thenReturn(200);

        filter.handle(request);
    }

    @Test
    public void shouldThrowBankServiceExceptionWhenSibsReturns429TooManyRequests() {
        Filter filter = new RateLimitErrorFilter();
        filter.setNext(stubFilter);

        int httpCode = 429;

        when(response.getStatus()).thenReturn(httpCode);
        when(response.getBody(String.class)).thenReturn(RATE_LIMIT_SIBS_MESSAGE);

        thrown.expect(BankServiceException.class);
        thrown.expectMessage(
                "Http status: " + httpCode + " Error body: " + RATE_LIMIT_SIBS_MESSAGE);

        filter.handle(request);
    }

    @Test
    public void shouldPassIfErrorIsNot429TooManyRequests() {
        Filter filter = new RateLimitErrorFilter();
        filter.setNext(stubFilter);

        when(response.getStatus()).thenReturn(200);

        filter.handle(request);
    }

    @Test
    public void shouldThrowBankServiceExceptionWhenSibsReturns405ServiceInvalid() {
        Filter filter = new ServiceInvalidErrorFilter();
        filter.setNext(stubFilter);

        int httpCode = 405;

        when(response.getStatus()).thenReturn(httpCode);
        when(response.getBody(String.class)).thenReturn(SERVICE_INVALID_SIBS_MESSAGE);

        thrown.expect(BankServiceException.class);
        thrown.expectMessage(
                "Http status: " + httpCode + " Error body: " + SERVICE_INVALID_SIBS_MESSAGE);

        filter.handle(request);
    }

    @Test
    public void shouldPassIfErrorIsNot405ServiceInvalid() {
        Filter filter = new ServiceInvalidErrorFilter();
        filter.setNext(stubFilter);

        when(response.getStatus()).thenReturn(200);

        filter.handle(request);
    }
}
