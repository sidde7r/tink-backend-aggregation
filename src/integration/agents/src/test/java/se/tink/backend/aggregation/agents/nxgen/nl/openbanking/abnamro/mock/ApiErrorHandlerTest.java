package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.mock.ApiErrorHandlerSampleResponses.ERROR_RESPONSE_401;
import static se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.mock.ApiErrorHandlerSampleResponses.ERROR_RESPONSE_429;
import static se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.mock.ApiErrorHandlerSampleResponses.ERROR_RESPONSE_503;
import static se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.mock.ApiErrorHandlerSampleResponses.OAUTH2_ERROR_RESPONSE_WITH_INVALID_GRANT;
import static se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.mock.ApiErrorHandlerSampleResponses.OAUTH2_ERROR_RESPONSE_WITH_UNKNOWN_ERROR;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import javax.ws.rs.core.MediaType;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator.rpc.OAuth2ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.errorhandling.ApiErrorHandler;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(JUnitParamsRunner.class)
public class ApiErrorHandlerTest {
    private static final Class<ConsentResponse> SAMPLE_RESPONSE_CLASS = ConsentResponse.class;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private RequestBuilder requestBuilder;

    @Before
    public void setUp() {
        requestBuilder = mock(RequestBuilder.class);
    }

    @Test
    public void shouldThrowBankServiceExceptionIfRequestReturns503StatusWithErrorBody()
            throws IOException {
        // given
        HttpResponse httpResponse =
                getHttpResponseForPostRequest(ERROR_RESPONSE_503, MediaType.APPLICATION_JSON_TYPE);
        when(httpResponse.getStatus()).thenReturn(503);

        // when
        final Throwable thrown =
                catchThrowable(
                        () ->
                                ApiErrorHandler.callWithErrorHandling(
                                        requestBuilder,
                                        SAMPLE_RESPONSE_CLASS,
                                        ApiErrorHandler.RequestType.POST));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(BankServiceException.class)
                .hasMessage("Service is currently unavailable. Please try after some time.");
    }

    @Test
    public void shouldThrowAccessExceededExceptionIfRequestReturns429StatusWithErrorBody()
            throws IOException {
        // given
        HttpResponse httpResponse = getHttpResponseForGetRequest(ERROR_RESPONSE_429);
        when(httpResponse.getStatus()).thenReturn(429);

        // when
        final Throwable thrown =
                catchThrowable(
                        () ->
                                ApiErrorHandler.callWithErrorHandling(
                                        requestBuilder,
                                        SAMPLE_RESPONSE_CLASS,
                                        ApiErrorHandler.RequestType.GET));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(BankServiceException.class)
                .hasMessage("Rate limit violation");
    }

    @Test
    public void shouldThrowSessionExpiredExceptionIfRequestReturns401StatusWithInvalidToken()
            throws IOException {
        // given
        HttpResponse httpResponse = getHttpResponseForGetRequest(ERROR_RESPONSE_401);
        when(httpResponse.getStatus()).thenReturn(401);

        // when
        final Throwable thrown =
                catchThrowable(
                        () ->
                                ApiErrorHandler.callWithErrorHandling(
                                        requestBuilder,
                                        SAMPLE_RESPONSE_CLASS,
                                        ApiErrorHandler.RequestType.GET));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
    }

    @Test
    @Parameters(
            method =
                    "parametersForShouldThrowSessionExpiredExceptionIfRequestReturns400StatusWithInvalidGrant")
    public void shouldThrowSessionExpiredExceptionIfRequestReturns400StatusWithInvalidGrant(
            MediaType mediaType) throws IOException {
        // given
        HttpResponse httpResponse =
                getHttpResponseForPostRequest(OAUTH2_ERROR_RESPONSE_WITH_INVALID_GRANT, mediaType);
        when(httpResponse.getStatus()).thenReturn(400);

        // when
        final Throwable thrown =
                catchThrowable(
                        () ->
                                ApiErrorHandler.callWithErrorHandling(
                                        requestBuilder,
                                        SAMPLE_RESPONSE_CLASS,
                                        ApiErrorHandler.RequestType.POST));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(SessionException.class)
                .hasMessage("Unknown, invalid, or expired refresh token");
    }

    @SuppressWarnings("unused")
    private Object[]
            parametersForShouldThrowSessionExpiredExceptionIfRequestReturns400StatusWithInvalidGrant() {
        return new Object[] {
            MediaType.APPLICATION_JSON_TYPE,
            new MediaType("application", "json", ImmutableMap.of("charset", "utf-8"))
        };
    }

    @Test
    public void shouldThrowHttpResponseExceptionIfRequestReturns400StatusWithUnknownOAuth2Error()
            throws IOException {
        // given
        HttpResponse httpResponse =
                getHttpResponseForPostRequest(
                        OAUTH2_ERROR_RESPONSE_WITH_UNKNOWN_ERROR, MediaType.APPLICATION_JSON_TYPE);
        when(httpResponse.getStatus()).thenReturn(400);

        // when
        final Throwable thrown =
                catchThrowable(
                        () ->
                                ApiErrorHandler.callWithErrorHandling(
                                        requestBuilder,
                                        SAMPLE_RESPONSE_CLASS,
                                        ApiErrorHandler.RequestType.POST));

        // then
        assertThat(thrown).isExactlyInstanceOf(HttpResponseException.class);
    }

    @Test
    @Parameters(
            method =
                    "parametersForShouldThrowHttpResponseExceptionIfRequestReturns400StatusWithUnexpectedBody")
    public void shouldThrowHttpResponseExceptionIfRequestReturns400StatusWithUnexpectedBody(
            String responseBody, MediaType mediaType) throws IOException {
        // given
        HttpResponse httpResponse = getHttpResponseForPostRequest(responseBody, mediaType);
        when(httpResponse.getStatus()).thenReturn(400);

        // when
        final Throwable thrown =
                catchThrowable(
                        () ->
                                ApiErrorHandler.callWithErrorHandling(
                                        requestBuilder,
                                        SAMPLE_RESPONSE_CLASS,
                                        ApiErrorHandler.RequestType.POST));

        // then
        assertThat(thrown).isExactlyInstanceOf(HttpResponseException.class);
    }

    @SuppressWarnings("unused")
    private Object[]
            parametersForShouldThrowHttpResponseExceptionIfRequestReturns400StatusWithUnexpectedBody() {
        return new Object[][] {
            {"{}", MediaType.APPLICATION_JSON_TYPE},
            {null, MediaType.APPLICATION_JSON_TYPE},
            {"{}", MediaType.TEXT_XML_TYPE}
        };
    }

    @Test
    public void shouldThrowRuntimeExceptionIfRequestThrowsRuntimeException() {
        // given
        when(requestBuilder.get(SAMPLE_RESPONSE_CLASS)).thenThrow(new RuntimeException());

        // when
        final Throwable thrown =
                catchThrowable(
                        () ->
                                ApiErrorHandler.callWithErrorHandling(
                                        requestBuilder,
                                        SAMPLE_RESPONSE_CLASS,
                                        ApiErrorHandler.RequestType.GET));

        // then
        assertThat(thrown).isExactlyInstanceOf(RuntimeException.class);
    }

    private HttpResponse getHttpResponseForPostRequest(String response, MediaType mediaType)
            throws IOException {
        HttpRequest httpRequest = mock(HttpRequest.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        HttpResponseException httpResponseException =
                new HttpResponseException(httpRequest, httpResponse);
        when(requestBuilder.post(SAMPLE_RESPONSE_CLASS)).thenThrow(httpResponseException);
        when(httpResponse.getType()).thenReturn(mediaType);
        prepareBodies(response, httpResponse);
        return httpResponse;
    }

    private HttpResponse getHttpResponseForGetRequest(String response) throws IOException {
        HttpRequest httpRequest = mock(HttpRequest.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        HttpResponseException httpResponseException =
                new HttpResponseException(httpRequest, httpResponse);
        when(requestBuilder.get(SAMPLE_RESPONSE_CLASS)).thenThrow(httpResponseException);
        when(httpResponse.getType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        prepareBodies(response, httpResponse);
        return httpResponse;
    }

    private void prepareBodies(String response, HttpResponse httpResponse) throws IOException {
        if (response != null) {
            OAuth2ErrorResponse oAuth2ErrorResponse =
                    OBJECT_MAPPER.readValue(response, OAuth2ErrorResponse.class);
            ErrorResponse errorResponse = OBJECT_MAPPER.readValue(response, ErrorResponse.class);
            when(httpResponse.getBody(OAuth2ErrorResponse.class)).thenReturn(oAuth2ErrorResponse);
            when(httpResponse.getBody(ErrorResponse.class)).thenReturn(errorResponse);
        }
    }
}
