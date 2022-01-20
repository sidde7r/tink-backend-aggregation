package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagErrorHandler.ErrorSource;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class BankverlagApiClientTest {
    private final String PASSWORD = "dummyPassword";
    private final String USERNAME = "dummyUsername";
    private final String OTP = "dummyOTP";
    private final String URL = "dummyUrl";

    private BankverlagApiClient apiClient;
    private BankverlagStorage storage;
    private BankverlagErrorHandler errorHandler;
    private BankverlagRequestBuilder requestBuilder;

    @Before
    public void init() {
        storage = mock(BankverlagStorage.class);
        errorHandler = mock(BankverlagErrorHandler.class);
        requestBuilder = mock(BankverlagRequestBuilder.class, RETURNS_DEEP_STUBS);

        apiClient =
                new BankverlagApiClient(
                        requestBuilder, storage, new ConstantLocalDateTimeSource(), errorHandler);
    }

    @Test
    public void initializeAuthorizationShouldSaveAuthMethodFromResponseHeaderIfHeaderAvailable() {
        // given
        HttpResponse mockResponse = mock(HttpResponse.class);
        given(requestBuilder.createRequest(any()).header(any(), any()).post(any(), any()))
                .willReturn(mockResponse);
        MultivaluedMap<String, String> headersMap = new MultivaluedMapImpl();
        headersMap.putSingle("Aspsp-Sca-Approach", "DECOUPLED");
        when(mockResponse.getHeaders()).thenReturn(headersMap);
        // when
        apiClient.initializeAuthorization(URL, USERNAME, PASSWORD);
        // then
        verify(storage).savePushOtpFromHeader();
    }

    @Test
    public void initializeAuthorizationShouldHandleErrorIfHttpResponseExceptionThrown() {
        // given
        given(requestBuilder.createRequest(any()).header(any(), any()).post(any(), any()))
                .willThrow(HttpResponseException.class);
        // when
        Throwable thrown =
                catchThrowable(() -> apiClient.initializeAuthorization(URL, USERNAME, PASSWORD));
        // then
        assertThat(thrown).isInstanceOf(HttpResponseException.class);
        verify(errorHandler)
                .handleError(
                        (HttpResponseException) thrown,
                        ErrorSource.AUTHORISATION_USERNAME_PASSWORD);
    }

    @Test
    public void finalizeAuthorizationShouldReturnAuthorizationResponse() {
        // given
        AuthorizationResponse mockResponse = mock(AuthorizationResponse.class);
        given(requestBuilder.createRequest(any()).put(any(), any())).willReturn(mockResponse);
        // when
        AuthorizationResponse response = apiClient.finalizeAuthorization(URL, OTP);
        // then
        assertThat(response).isEqualTo(mockResponse);
    }

    @Test
    public void finalizeAuthorizationShouldHandleErrorIfHttpResponseExceptionThrown() {
        // given
        given(requestBuilder.createRequest(any()).put(any(), any()))
                .willThrow(HttpResponseException.class);
        // when
        Throwable thrown = catchThrowable(() -> apiClient.finalizeAuthorization(URL, OTP));
        // then
        assertThat(thrown).isInstanceOf(HttpResponseException.class);
        verify(errorHandler)
                .handleError(
                        (HttpResponseException) thrown,
                        ErrorSource.AUTHORISATION_USERNAME_PASSWORD);
    }
}
