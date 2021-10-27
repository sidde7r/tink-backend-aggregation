package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Scope;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.DecoupledResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(MockitoJUnitRunner.class)
public class HandelsbankenBankIdAuthenticatorTest {

    private static final String REFRESH_TOKEN = "dummyRefreshToken";
    private static final String SSN = "200001011101";

    private HandelsbankenBankIdAuthenticator authenticator;
    private HandelsbankenBaseApiClient apiClient;
    private SessionResponse reference;

    @Mock private HttpResponse httpResponse;
    @Mock private HttpResponseException httpResponseException;

    @Before
    public void setup() {
        SessionStorage sessionStorage = new SessionStorage();
        this.apiClient = mock(HandelsbankenBaseApiClient.class);
        this.authenticator = new HandelsbankenBankIdAuthenticator(apiClient, sessionStorage);
        this.reference = getSessionResponse();
    }

    @Test
    public void shouldThrowLoginErrorIncorrectCredentialsIfSsnIsNullOrEmpty() {
        assertThatThrownBy(() -> authenticator.init(""))
                .isInstanceOf(LoginError.INCORRECT_CREDENTIALS.exception().getClass());
        assertThatThrownBy(() -> authenticator.init(null))
                .isInstanceOf(LoginError.INCORRECT_CREDENTIALS.exception().getClass());
    }

    @Test
    public void initShouldThrowBankIdErrorUnknownWhenHttpClientExceptionIsCaught() {
        // when
        when(apiClient.requestClientCredentialGrantTokenWithScope(Scope.AIS))
                .thenThrow(HttpClientException.class);

        // then
        assertThatThrownBy(() -> authenticator.init(SSN))
                .isInstanceOf(BankIdError.UNKNOWN.exception().getClass());
    }

    @Test
    public void initShouldThrowLoginErrorIncorrectCredentialsWhenResponseIsInvalidRequest() {
        // given
        given(httpResponse.getStatus()).willReturn(HttpStatus.SC_BAD_REQUEST);
        given(httpResponse.getBody(ErrorResponse.class))
                .willReturn(getInitInvalidRequestErrorResponse());
        given(httpResponseException.getResponse()).willReturn(httpResponse);

        // when
        when(apiClient.requestClientCredentialGrantTokenWithScope(Scope.AIS))
                .thenThrow(httpResponseException);

        // then
        assertThatThrownBy(() -> authenticator.init(SSN))
                .isInstanceOf(LoginError.INCORRECT_CREDENTIALS.exception().getClass());
    }

    @Test
    public void initShouldReThrowExceptionWhenErrorResponseHasNoError() {
        // given
        given(httpResponse.getStatus()).willReturn(HttpStatus.SC_BAD_REQUEST);
        given(httpResponse.getBody(ErrorResponse.class))
                .willReturn(getInitErrorResponseWithoutError());
        given(httpResponseException.getResponse()).willReturn(httpResponse);

        // when
        when(apiClient.requestClientCredentialGrantTokenWithScope(Scope.AIS))
                .thenThrow(httpResponseException);

        // then
        assertThatThrownBy(() -> authenticator.init(SSN)).isSameAs(httpResponseException);
    }

    @Test
    public void collectShouldReturnBankIdStatusWaitingWhenResultIsInProgress() {
        // when
        when(apiClient.getDecoupled(any())).thenReturn(getInProgressDecoupledResponse());

        // then
        assertEquals(BankIdStatus.WAITING, authenticator.collect(reference));
    }

    @Test
    public void collectShouldReturnBankIdStatusUnknownWhenResultIsUnknown() {
        // when
        when(apiClient.getDecoupled(any())).thenReturn(getUnknownDecoupledResponse());

        // then
        assertEquals(BankIdStatus.FAILED_UNKNOWN, authenticator.collect(reference));
    }

    @Test
    public void collectShouldReturnBankIdStatusTimeoutWhenErrorIsTimeout() {
        // when
        when(apiClient.getDecoupled(any())).thenReturn(getTimeoutErrorDecoupledResponse());

        // then
        assertEquals(BankIdStatus.TIMEOUT, authenticator.collect(reference));
    }

    @Test
    public void collectShouldThrowBankIdStatusUnknownWhenErrorIsUnknown() {
        // when
        when(apiClient.getDecoupled(any())).thenReturn(getUnknownErrorDecoupledResponse());

        // then
        assertEquals(BankIdStatus.FAILED_UNKNOWN, authenticator.collect(reference));
    }

    @Test
    public void refreshAccessTokenShouldThrowSessionExpiredWhenResponseIsBadRequest() {
        // given
        given(httpResponse.getStatus()).willReturn(HttpStatus.SC_BAD_REQUEST);
        given(httpResponseException.getResponse()).willReturn(httpResponse);

        // when
        when(apiClient.getRefreshToken(REFRESH_TOKEN)).thenThrow(httpResponseException);

        // then
        assertThatThrownBy(() -> authenticator.refreshAccessToken(REFRESH_TOKEN))
                .isInstanceOf(SessionError.SESSION_EXPIRED.exception().getClass());
    }

    @Test
    public void refreshAccessTokenShouldReturnOptionalEmptyWhenErrorResponseIsUnknown() {
        // given
        given(httpResponse.getStatus()).willReturn(HttpStatus.SC_CONFLICT);
        given(httpResponseException.getResponse()).willReturn(httpResponse);

        // when
        when(apiClient.getRefreshToken(REFRESH_TOKEN)).thenThrow(httpResponseException);

        // then
        assertEquals(Optional.empty(), authenticator.refreshAccessToken(REFRESH_TOKEN));
    }

    private SessionResponse getSessionResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"auto_start_token\": \"abcdabcd-7a8f-47e4-b3ae-abcdabcdabcd\",\n"
                        + "  \"sleep_time\": 2000,\n"
                        + "  \"_links\": {\n"
                        + "    \"token\": {\n"
                        + "      \"href\": \"https://api.handelsbanken.com/bb/gls5/decoupled/mbid/token/1.0?sessionId=sessionId\",\n"
                        + "      \"hints\": {\n"
                        + "        \"allow\": [\n"
                        + "          \"POST\"\n"
                        + "        ]\n"
                        + "      }\n"
                        + "    },\n"
                        + "    \"cancel\": {\n"
                        + "      \"href\": \"https://api.handelsbanken.com/bb/gls5/decoupled/mbid/cancel/1.0?sessionId=sessionId\",\n"
                        + "      \"hints\": {\n"
                        + "        \"allow\": [\n"
                        + "          \"POST\"\n"
                        + "        ]\n"
                        + "      }\n"
                        + "    }\n"
                        + "  }\n"
                        + "}",
                SessionResponse.class);
    }

    private ErrorResponse getInitInvalidRequestErrorResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n" + "  \"error\": \"invalid_request\"\n" + "}", ErrorResponse.class);
    }

    private ErrorResponse getInitErrorResponseWithoutError() {
        return SerializationUtils.deserializeFromString(
                "{\n" + "  \"unknownField\": \"message\"\n" + "}", ErrorResponse.class);
    }

    private DecoupledResponse getInProgressDecoupledResponse() {
        return SerializationUtils.deserializeFromString(
                "{\"result\":\"IN_PROGRESS\"}", DecoupledResponse.class);
    }

    private DecoupledResponse getUnknownDecoupledResponse() {
        return SerializationUtils.deserializeFromString(
                "{\"result\":\"UNKNOWN\"}", DecoupledResponse.class);
    }

    private DecoupledResponse getTimeoutErrorDecoupledResponse() {
        return SerializationUtils.deserializeFromString(
                "{\"error\":\"mbid_max_polling\"}", DecoupledResponse.class);
    }

    private DecoupledResponse getUnknownErrorDecoupledResponse() {
        return SerializationUtils.deserializeFromString(
                "{\"error\":\"UNKNOWN\"}", DecoupledResponse.class);
    }
}
