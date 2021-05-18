package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.Collections;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants.ErrorMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants.Headers;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(JUnitParamsRunner.class)
public class SdcExceptionFilterTest {

    private static final int HTTP_OK = 200;
    private static final int HTTP_UNAUTHORIZED = 401;

    private static final String UNKNOWN_MSG = "Unknown Msg";
    private static final String UNKNOWN_HEADER = "Unknown header";
    private static final String YOUR_PIN_CODE_IS_BLOCKED = "Your PIN code is blocked.";
    private static final String PIN_LENGTH_ERROR = "Your PIN must contain exactly 4 characters.";
    private static final String OTP_LENGTH_ERROR = "Your OTP must contain exactly 4 characters.";

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
    @Parameters(method = "knownMessages")
    public void shouldHandleRequestWithResponseGreaterOrEqual400WithKnownHeaderAndKnownMsg(
            String message, String reason) {
        // given
        HttpResponse mockHttpResponse = Mockito.mock(HttpResponse.class);
        when(mockHttpResponse.getStatus()).thenReturn(HTTP_UNAUTHORIZED);
        when(mockHttpResponse.getHeaders())
                .thenReturn(createHeaderMap(Headers.X_SDC_ERROR_MESSAGE, message));
        when(mockFilter.handle(httpRequest)).thenReturn(mockHttpResponse);

        // when
        final Throwable thrown = catchThrowable(() -> sdcExceptionFilter.handle(httpRequest));

        // then
        assertThat(thrown).isExactlyInstanceOf(LoginException.class).hasMessageContaining(reason);
        assertThat(thrown.getLocalizedMessage()).isEqualTo(reason);
    }

    private Object[] knownMessages() {
        return new Object[] {
            new Object[] {ErrorMessage.PIN_BLOCKED.getCriteria(), YOUR_PIN_CODE_IS_BLOCKED},
            new Object[] {ErrorMessage.PIN_4_CHARACTERS_EN.getCriteria(), PIN_LENGTH_ERROR}
        };
    }

    @Test
    @Parameters(method = "configuredMessages")
    public void shouldHandlePassedMessages(String message, LoginError error, String reason) {
        // given
        HttpResponse mockHttpResponse = Mockito.mock(HttpResponse.class);
        when(mockHttpResponse.getStatus()).thenReturn(HTTP_UNAUTHORIZED);
        when(mockHttpResponse.getHeaders())
                .thenReturn(createHeaderMap(Headers.X_SDC_ERROR_MESSAGE, message));
        when(mockFilter.handle(httpRequest)).thenReturn(mockHttpResponse);
        sdcExceptionFilter =
                new SdcExceptionFilter(
                        Collections.singletonMap(message, new ImmutablePair<>(error, reason)));
        sdcExceptionFilter.setNext(mockFilter);

        // when
        final Throwable thrown = catchThrowable(() -> sdcExceptionFilter.handle(httpRequest));

        // then
        assertThat(thrown).isExactlyInstanceOf(error.exception().getClass());
        if (reason == null) {
            assertThat(thrown.getMessage()).contains(error.name());
        } else {
            assertThat(thrown.getMessage()).contains(reason);
        }
    }

    private Object[] configuredMessages() {
        return new Object[] {
            new Object[] {"message", LoginError.INCORRECT_CREDENTIALS, "reason"},
            new Object[] {"messageWithNoReason", LoginError.INCORRECT_CREDENTIALS, null}
        };
    }

    @Test
    public void shouldHandleRequestWithResponseGreaterOrEqual400WithUnknownHeaderAndKnownBody() {
        // given
        HttpResponse mockHttpResponse = Mockito.mock(HttpResponse.class);
        when(mockHttpResponse.getStatus()).thenReturn(HTTP_UNAUTHORIZED);
        when(mockHttpResponse.getHeaders())
                .thenReturn(createHeaderMap(UNKNOWN_HEADER, UNKNOWN_MSG));
        when(mockHttpResponse.getBody(String.class))
                .thenReturn(ErrorMessage.PIN_4_CHARACTERS_EN.getCriteria());
        when(mockFilter.handle(httpRequest)).thenReturn(mockHttpResponse);

        // when
        final Throwable thrown = catchThrowable(() -> sdcExceptionFilter.handle(httpRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessageContaining(PIN_LENGTH_ERROR);
    }

    @Test
    public void shouldHandleResponseWithTooShortOtpErrorMessage() {
        // given
        HttpResponse mockHttpResponse = Mockito.mock(HttpResponse.class);
        when(mockHttpResponse.getStatus()).thenReturn(HTTP_UNAUTHORIZED);
        when(mockHttpResponse.getHeaders())
                .thenReturn(createHeaderMap(UNKNOWN_HEADER, UNKNOWN_MSG));
        when(mockHttpResponse.getBody(String.class))
                .thenReturn(ErrorMessage.OTP_4_CHARACTERS_DK.getCriteria());
        when(mockFilter.handle(httpRequest)).thenReturn(mockHttpResponse);

        // when
        final Throwable thrown = catchThrowable(() -> sdcExceptionFilter.handle(httpRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessageContaining(OTP_LENGTH_ERROR);
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

    @Test
    public void canCreateFilterWithCustomConfiguration() {
        // given
        Map<String, Pair<LoginError, String>> mapping =
                new ImmutableMap.Builder<String, Pair<LoginError, String>>()
                        .put(
                                "Forkert brugernummer eller adgangskode.",
                                // Incorrect user number or password.
                                new ImmutablePair<>(LoginError.INCORRECT_CREDENTIALS, null))
                        .put(
                                "Din PIN kode er spærret. Vælg \"Tilmeld ny aftale\" i menuen (de tre prikker) for at genaktivere din aftale",
                                // Your PIN is blocked. Select "Register new appointment" in the
                                // menu (the three dots) to reactivate your appointment
                                new ImmutablePair<>(
                                        LoginError.INCORRECT_CREDENTIALS,
                                        "Your PIN code is blocked."))
                        .build();
        // when
        final Throwable thrown =
                catchThrowable(() -> sdcExceptionFilter = new SdcExceptionFilter(mapping));

        // then
        assertThat(thrown).isNull();
    }

    private MultivaluedMap<String, String> createHeaderMap(String header, String msg) {
        MultivaluedMapImpl headers = new MultivaluedMapImpl();
        headers.add(header, msg);
        return headers;
    }
}
