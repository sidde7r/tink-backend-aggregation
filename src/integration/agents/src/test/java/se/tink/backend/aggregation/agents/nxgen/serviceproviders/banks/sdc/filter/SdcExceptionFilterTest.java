package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants.ErrorMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants.Headers;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SdcExceptionFilterTest {

    private static final int HTTP_OK = 200;
    private static final int HTTP_UNAUTHORIZED = 401;

    private static final String UNKNOWN_MSG = "Unknown Msg";
    private static final String UNKNOWN_HEADER = "Unknown header";
    private static final String YOUR_PIN_CODE_IS_BLOCKED = "Your PIN code is blocked.";
    private static final String YOUR_PIN_HAS_ILLEGAL_CHARACTERS =
            "Your PIN has illegal characters.";

    private final HttpRequest httpRequest = Mockito.mock(HttpRequest.class);

    private SdcExceptionFilter sdcExceptionFilter;
    private Filter mockFilter;

    @Before
    public void setUp() {
        mockFilter = mock(Filter.class);
        sdcExceptionFilter = new SdcExceptionFilter();
        sdcExceptionFilter.setNext(mockFilter);
    }

    @Test
    public void shouldHandleRequestWithResponseBelow400() {
        // given
        HttpResponse mockHttpResponse = Mockito.mock(HttpResponse.class);
        when(mockHttpResponse.getStatus()).thenReturn(HTTP_OK);
        when(mockFilter.handle(httpRequest)).thenReturn(mockHttpResponse);

        // when
        HttpResponse filterHttpResponse = sdcExceptionFilter.handle(httpRequest);

        // then
        assertThat(mockHttpResponse.getStatus()).isEqualTo(filterHttpResponse.getStatus());
    }

    @Test
    public void shouldHandleRequestWithResponseGreaterOrEqual400WithKnownHeaderAndUnknownMsg() {
        // given
        HttpResponse mockHttpResponse = Mockito.mock(HttpResponse.class);
        when(mockHttpResponse.getStatus()).thenReturn(HTTP_UNAUTHORIZED);
        when(mockHttpResponse.getHeaders())
                .thenReturn(createHeaderMap(Headers.X_SDC_ERROR_MESSAGE, UNKNOWN_MSG));
        when(mockFilter.handle(httpRequest)).thenReturn(mockHttpResponse);

        // when
        final Throwable thrown = catchThrowable(() -> sdcExceptionFilter.handle(httpRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(HttpResponseException.class)
                .hasMessageContaining(
                        "with " + Headers.X_SDC_ERROR_MESSAGE + " header: " + UNKNOWN_MSG);
    }

    @Test
    public void shouldHandleRequestWithResponseGreaterOrEqual400WithKnownHeaderAndKnownMsg() {
        // given
        HttpResponse mockHttpResponse = Mockito.mock(HttpResponse.class);
        when(mockHttpResponse.getStatus()).thenReturn(HTTP_UNAUTHORIZED);
        when(mockHttpResponse.getHeaders())
                .thenReturn(
                        createHeaderMap(
                                Headers.X_SDC_ERROR_MESSAGE,
                                ErrorMessage.PIN_BLOCKED.getCriteria()));
        when(mockFilter.handle(httpRequest)).thenReturn(mockHttpResponse);

        // when
        final Throwable thrown = catchThrowable(() -> sdcExceptionFilter.handle(httpRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessageContaining(YOUR_PIN_CODE_IS_BLOCKED);
    }

    @Test
    public void shouldHandleRequestWithResponseGreaterOrEqual400WithUnknownHeaderAndKnownBody() {
        // given
        HttpResponse mockHttpResponse = Mockito.mock(HttpResponse.class);
        when(mockHttpResponse.getStatus()).thenReturn(HTTP_UNAUTHORIZED);
        when(mockHttpResponse.getHeaders())
                .thenReturn(createHeaderMap(UNKNOWN_HEADER, UNKNOWN_MSG));
        when(mockHttpResponse.getBody(String.class))
                .thenReturn(ErrorMessage.PIN_4_CHARACTERS.getCriteria());
        when(mockFilter.handle(httpRequest)).thenReturn(mockHttpResponse);

        // when
        final Throwable thrown = catchThrowable(() -> sdcExceptionFilter.handle(httpRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessageContaining(YOUR_PIN_HAS_ILLEGAL_CHARACTERS);
    }

    @Test
    public void shouldHandleRequestWithResponseGreaterOrEqual400WithUnknownHeader() {
        // given
        HttpResponse mockHttpResponse = Mockito.mock(HttpResponse.class);
        when(mockHttpResponse.getStatus()).thenReturn(HTTP_UNAUTHORIZED);
        when(mockHttpResponse.getHeaders())
                .thenReturn(createHeaderMap(UNKNOWN_HEADER, UNKNOWN_MSG));
        when(mockFilter.handle(httpRequest)).thenReturn(mockHttpResponse);

        // when
        final Throwable thrown = catchThrowable(() -> sdcExceptionFilter.handle(httpRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(HttpResponseException.class)
                .hasMessageContaining("with " + Headers.X_SDC_ERROR_MESSAGE + " header: null");
    }

    private MultivaluedMap<String, String> createHeaderMap(String header, String msg) {
        MultivaluedMapImpl headers = new MultivaluedMapImpl();
        headers.add(header, msg);
        return headers;
    }
}
