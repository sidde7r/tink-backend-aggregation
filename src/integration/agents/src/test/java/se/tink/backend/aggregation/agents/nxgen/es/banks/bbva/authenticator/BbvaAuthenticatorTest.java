package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.BbvaErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.transactionsdatefrommanager.AccountsProvider;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.transactionsdatefrommanager.TransactionsFetchingDateFromManager;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BbvaAuthenticatorTest {

    private Credentials credentials;
    private BbvaAuthenticator authenticator;
    private BbvaApiClient apiClient;
    private TransactionPaginationHelper transactionPaginationHelper;
    private PersistentStorage persistentStorage;

    @Before
    public void setUp() {
        apiClient = mock(BbvaApiClient.class);
        credentials = new Credentials();
        credentials.setUsername("username");
        credentials.setPassword("password");
        authenticator =
                new BbvaAuthenticator(
                        apiClient,
                        mock(SupplementalInformationHelper.class),
                        mock(CredentialsRequest.class),
                        mock(TransactionsFetchingDateFromManager.class),
                        mock(AccountsProvider.class));
    }

    @Test
    public void shouldThrowBankServiceExceptionWhenBadGateway() {
        // given
        HttpResponseException exception = mockResponse(502);
        when(apiClient.login(any())).thenThrow(exception);

        // when
        Throwable thrown = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(BankServiceException.class)
                .hasMessage("Cause: BankServiceError.BANK_SIDE_FAILURE");
    }

    @Test
    public void shouldThrowBankServiceExceptionWhenInternalServerError() {
        // given
        HttpResponseException exception = mockResponse(500);
        when(apiClient.login(any())).thenThrow(exception);

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
    public void shouldThrowLoginExceptionWhenNoCustomer() {
        // given
        HttpResponseException exception = mockResponse(409);
        when(apiClient.login(any())).thenThrow(exception);

        // when
        Throwable thrown = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.NOT_CUSTOMER");
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
        HttpResponseException exception = mockResponse(404);
        when(apiClient.login(any())).thenThrow(exception);

        // when
        Throwable thrown = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Unknown error: httpStatus 404, code 1, message Test message");
    }

    @Test
    public void shouldThrowIncorrectChallengeErrorWhenReceiveUnauthorizedOtpResponse() {
        // given
        HttpResponseException exception =
                mockOtpErrorResponse(401, "El otp no coincide con el generado");
        doThrow(exception).when(apiClient).requestMoreThan90DaysTransactionsForFirstAccount(any());

        // when
        Throwable thrown = catchThrowable(authenticator::forcedOtpForExtendedPeriod);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CHALLENGE_RESPONSE");
    }

    private HttpResponseException mockResponse(int status) {
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getStatus()).thenReturn(status);
        HttpResponseException exception = new HttpResponseException(null, mocked);

        when(exception.getResponse().getBody(BbvaErrorResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                "{\n"
                                        + "    \"consumer-request-id\": \"E\",\n"
                                        + "    \"error-code\": \"1\",\n"
                                        + "    \"error-message\": \"Test message\",\n"
                                        + "    \"http-status\": "
                                        + status
                                        + ",\n"
                                        + "    \"severity\": \"FATAL\",\n"
                                        + "    \"system-error-code\": \"errorCode\",\n"
                                        + "    \"system-error-description\": \"Error description\",\n"
                                        + "    \"version\": 1\n"
                                        + "}\n",
                                BbvaErrorResponse.class));

        return exception;
    }

    private HttpResponseException mockOtpErrorResponse(int status, String errorMessage) {
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getStatus()).thenReturn(status);
        HttpResponseException exception = new HttpResponseException(null, mocked);

        when(exception.getResponse().getBody(BbvaErrorResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                "{\n"
                                        + "    \"version\": 1,\n"
                                        + "    \"severity\": \"FATAL\",\n"
                                        + "    \"http-status\": "
                                        + status
                                        + ",\n"
                                        + "    \"error-code\": \"168\",\n"
                                        + "    \"error-message\": \""
                                        + errorMessage
                                        + "\",\n"
                                        + "    \"consumer-request-id\": \"31e5d8cd-cd9c-4f95-8230-a107e7f4e686\",\n"
                                        + "    \"system-error-code\": \"unauthorized\",\n"
                                        + "    \"system-error-description\": \""
                                        + errorMessage
                                        + "\",\n"
                                        + "    \"system-error-cause\": \"errorSAS=168, errorHTTP=401, errorMessage="
                                        + errorMessage
                                        + ", exceptionMessage=The OTP generated does not match the OTP sent by the user\"\n"
                                        + "}",
                                BbvaErrorResponse.class));

        return exception;
    }

    @Test
    public void shouldThrowLoginExceptionWhenCustomerUsernameIsInvalidSpanishDNI() {
        // given
        apiClient = mock(BbvaApiClient.class);
        credentials = new Credentials();
        credentials.setUsername("32X");
        credentials.setPassword("password");
        authenticator =
                new BbvaAuthenticator(
                        apiClient,
                        mock(SupplementalInformationHelper.class),
                        mock(CredentialsRequest.class),
                        mock(TransactionsFetchingDateFromManager.class),
                        mock(AccountsProvider.class));

        // when
        Throwable thrown = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Username with invalid format");
    }
}
