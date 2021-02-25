package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.jackson.datatype.VavrModule;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.authenticator.rpc.EnrollmentResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.rpc.CajamarErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CajamarAuthenticatorTest {

    static final String DATA_PATH = "data/test/agents/es/cajamar/";
    private CajamarApiClient apiClient;
    private CajamarAuthenticator authenticator;
    private Credentials credentials;

    @Before
    public void setup() {
        apiClient = mock(CajamarApiClient.class);
        credentials = new Credentials();
        credentials.setUsername("username");
        credentials.setPassword("password");
        authenticator = new CajamarAuthenticator(apiClient);
    }

    @Test
    public void shouldLoginWithoutError() throws IOException {
        // given
        EnrollmentResponse enrollmentResponse =
                loadSampleData("enrollment_response.json", EnrollmentResponse.class);
        LoginResponse loginResponse = loadSampleData("login_response.json", LoginResponse.class);
        when(apiClient.fetchEnrollment(any())).thenReturn(enrollmentResponse);
        when(apiClient.login(any())).thenReturn(loginResponse);

        // when
        Throwable thrown = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(thrown).isEqualTo(null);
        assertEquals(enrollmentResponse.getAccessToken(), "access_token");
        assertEquals(loginResponse.getName(), "STEVE JOBS");
    }

    @Test
    public void shouldThrowBankServiceErrorWhenInternalServiceError() {
        // given
        HttpResponseException exception = mockResponse(502);
        when(apiClient.fetchEnrollment(any())).thenThrow(exception);

        // when
        Throwable thrown = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(BankServiceException.class)
                .hasMessage("Cause: BankServiceError.BANK_SIDE_FAILURE");
    }

    @Test
    public void shouldThrowAuthorizationExceptionWhenUnauthorized() {
        // given
        HttpResponseException exception = mockResponse(401);

        // when
        when(apiClient.login(any())).thenThrow(exception);

        // then
        Throwable thrown = catchThrowable(() -> authenticator.authenticate(credentials));
        assertThat(thrown)
                .isExactlyInstanceOf(AuthorizationException.class)
                .hasMessage("Cause: AuthorizationError.UNAUTHORIZED");
    }

    @Test
    public void shouldThrowLoginExceptionWhenForbidden() {
        // given
        HttpResponseException exception = mockResponse(403);
        when(apiClient.login(any())).thenThrow(exception);

        // when
        Throwable thrown = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void shouldThrowLoginExceptionWhenNotFound() {
        // given
        HttpResponseException exception = mockResponse(409);
        when(apiClient.login(any())).thenThrow(exception);

        // when
        Throwable thrown = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Error message: httpStatus: 409, code: SYS01, message: Error");
    }

    private HttpResponseException mockResponse(int status) {
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getStatus()).thenReturn(status);
        HttpResponseException exception = new HttpResponseException(null, mocked);

        when(exception.getResponse().getBody(CajamarErrorResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"code\": \"SYS01\",\"message\": \"Error\"}",
                                CajamarErrorResponse.class));

        return exception;
    }

    private <T> T loadSampleData(String path, Class<T> cls) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new VavrModule());
        return objectMapper.readValue(Paths.get(DATA_PATH, path).toFile(), cls);
    }
}
