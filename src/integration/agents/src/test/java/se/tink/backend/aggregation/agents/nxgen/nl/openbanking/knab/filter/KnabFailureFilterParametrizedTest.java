package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.filter;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RunWith(JUnitParamsRunner.class)
public class KnabFailureFilterParametrizedTest {

    private KnabFailureFilter knabFailureFilter;
    private Filter nextFilter;
    private HttpRequest httpRequest;

    @Before
    public void setUp() {
        httpRequest = mock(HttpRequest.class);
        nextFilter = mock(Filter.class);
        knabFailureFilter = new KnabFailureFilter();
    }

    @Test
    @Parameters(method = "getResponseCodes")
    public void shouldReturnHttpResponseIfStatusIsDifferentThanForbidden(int responseCode) {
        // given
        HttpResponse mockedResponse = FilterTestHelperUtility.mockResponse(responseCode, null);

        // when
        knabFailureFilter.setNext(nextFilter);
        when(nextFilter.handle(any(HttpRequest.class))).thenReturn(mockedResponse);

        // then
        assertEquals(knabFailureFilter.handle(httpRequest), mockedResponse);
    }

    private Object[] getResponseCodes() {
        return new Object[] {
            HttpStatus.SC_CONTINUE,
            HttpStatus.SC_SWITCHING_PROTOCOLS,
            HttpStatus.SC_PROCESSING,
            HttpStatus.SC_OK,
            HttpStatus.SC_CREATED,
            HttpStatus.SC_ACCEPTED,
            HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION,
            HttpStatus.SC_NO_CONTENT,
            HttpStatus.SC_RESET_CONTENT,
            HttpStatus.SC_PARTIAL_CONTENT,
            HttpStatus.SC_MULTI_STATUS,
            HttpStatus.SC_MULTIPLE_CHOICES,
            HttpStatus.SC_MOVED_PERMANENTLY,
            HttpStatus.SC_MOVED_TEMPORARILY,
            HttpStatus.SC_SEE_OTHER,
            HttpStatus.SC_NOT_MODIFIED,
            HttpStatus.SC_USE_PROXY,
            HttpStatus.SC_TEMPORARY_REDIRECT,
            HttpStatus.SC_BAD_REQUEST,
            HttpStatus.SC_UNAUTHORIZED,
            HttpStatus.SC_PAYMENT_REQUIRED,
            // HttpStatus.SC_FORBIDDEN left for highlight that is not used
            HttpStatus.SC_NOT_FOUND,
            HttpStatus.SC_METHOD_NOT_ALLOWED,
            HttpStatus.SC_NOT_ACCEPTABLE,
            HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED,
            HttpStatus.SC_REQUEST_TIMEOUT,
            HttpStatus.SC_CONFLICT,
            HttpStatus.SC_GONE,
            HttpStatus.SC_LENGTH_REQUIRED,
            HttpStatus.SC_PRECONDITION_FAILED,
            HttpStatus.SC_REQUEST_TOO_LONG,
            HttpStatus.SC_REQUEST_URI_TOO_LONG,
            HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE,
            HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE,
            HttpStatus.SC_EXPECTATION_FAILED,
            HttpStatus.SC_INSUFFICIENT_SPACE_ON_RESOURCE,
            HttpStatus.SC_METHOD_FAILURE,
            HttpStatus.SC_UNPROCESSABLE_ENTITY,
            HttpStatus.SC_LOCKED,
            HttpStatus.SC_FAILED_DEPENDENCY,
            HttpStatus.SC_INTERNAL_SERVER_ERROR,
            HttpStatus.SC_NOT_IMPLEMENTED,
            HttpStatus.SC_BAD_GATEWAY,
            HttpStatus.SC_SERVICE_UNAVAILABLE,
            HttpStatus.SC_GATEWAY_TIMEOUT,
            HttpStatus.SC_HTTP_VERSION_NOT_SUPPORTED,
            HttpStatus.SC_INSUFFICIENT_STORAGE
        };
    }
}
