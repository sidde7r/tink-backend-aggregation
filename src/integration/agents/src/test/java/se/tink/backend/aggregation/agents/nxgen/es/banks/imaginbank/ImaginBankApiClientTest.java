package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.ErrorCode;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.rpc.ImaginBankErrorResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class ImaginBankApiClientTest {
    private static final String SUBMIT_LOGIN = "https://api.imagin.com/xmlapps/rest/login/login";
    private static final String INITIATE_CARD_FETCHING =
            "https://api.imagin.com/xmlapps/rest/finanbox/inicializarBoxes";

    private static final LoginRequest loginRequest =
            new LoginRequest("username", "userType", true, "password", false, false);

    private static Object accountBlockedErrorCodes() {
        return ErrorCode.ACCOUNT_BLOCKED;
    }

    private static final String UNKNOWN_ERROR_CODE = "666";

    private ImaginBankApiClient imaginApiClient;
    private TinkHttpClient httpClientMock;

    @Before
    public void setUp() {
        httpClientMock = mock(TinkHttpClient.class);
        imaginApiClient = new ImaginBankApiClient(httpClientMock);
    }

    @Test
    public void shouldThrowLoginExceptionWhenIncorrectCredentials() {
        // given
        setupHttpClientMockForAuthenticationExceptionByErrorCode(ErrorCode.INCORRECT_CREDENTIALS);
        // then
        Throwable thrown = catchThrowable(() -> imaginApiClient.login(loginRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    @Parameters(method = "accountBlockedErrorCodes")
    public void shouldThrowAuthorizationExceptionWhenAccountBlocked(String errorCode) {
        // given
        setupHttpClientMockForAuthenticationExceptionByErrorCode(errorCode);
        // then
        Throwable thrown = catchThrowable(() -> imaginApiClient.login(loginRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(AuthorizationException.class)
                .hasMessage("Cause: AuthorizationError.ACCOUNT_BLOCKED");
    }

    @Test
    public void shouldThrowLoginExceptionForUnknownErrorCode() {
        // given
        setupHttpClientMockForAuthenticationExceptionByErrorCode(UNKNOWN_ERROR_CODE);
        // then
        Throwable thrown = catchThrowable(() -> imaginApiClient.login(loginRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.DEFAULT_MESSAGE");
    }

    @Test
    public void shouldThrowBankServiceExceptionWhenUnavailable() {
        // given
        setupHttpClientMockForInitiateCardFetchingExceptionByErrorCode(ErrorCode.UNAVAILABLE);
        // then
        Throwable thrown = catchThrowable(() -> imaginApiClient.initiateCardFetching());

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(BankServiceException.class)
                .hasMessage("Cause: BankServiceError.BANK_SIDE_FAILURE");
    }

    private void setupHttpClientMockForAuthenticationExceptionByErrorCode(String errorCode) {
        final HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(409);
        when(httpResponse.getBody(ImaginBankErrorResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"codigo\":\"" + errorCode + "\",\"mensaje\":\"ERROR\"}",
                                ImaginBankErrorResponse.class));
        when(httpClientMock.request(new URL(SUBMIT_LOGIN)))
                .thenThrow(new HttpResponseException(null, httpResponse));
    }

    private void setupHttpClientMockForInitiateCardFetchingExceptionByErrorCode(String errorCode) {
        final HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(409);
        when(httpResponse.getBody(ImaginBankErrorResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"codigo\":\"" + errorCode + "\",\"mensaje\":\"ERROR\"}",
                                ImaginBankErrorResponse.class));
        when(httpClientMock.request(new URL(INITIATE_CARD_FETCHING)))
                .thenThrow(new HttpResponseException(null, httpResponse));
    }
}
