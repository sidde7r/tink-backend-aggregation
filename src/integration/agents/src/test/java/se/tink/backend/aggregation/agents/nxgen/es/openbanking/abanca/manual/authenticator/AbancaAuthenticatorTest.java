package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.manual.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.authenticator.AbancaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.configuration.AbancaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AbancaAuthenticatorTest {

    private AbancaApiClient apiClient;
    private AbancaConfiguration abancaConfiguration;
    private UserAvailability userAvailability;
    private AbancaAuthenticator authenticator;

    @Before
    public void setup() {
        apiClient = mock(AbancaApiClient.class);
        abancaConfiguration = mock(AbancaConfiguration.class);
        userAvailability = mock(UserAvailability.class);
        authenticator = new AbancaAuthenticator(apiClient, abancaConfiguration, userAvailability);
    }

    @Test
    public void shouldAbortSCAAuthentication() {
        // given
        OAuth2Token token = OAuth2Token.create("type", "accessToken", "refreshToken", 500);
        String clientId = "1234";

        when(userAvailability.isUserAvailableForInteraction()).thenReturn(false);
        when(abancaConfiguration.getClientId()).thenReturn(clientId);
        when(apiClient.getToken(any())).thenReturn(token);

        // when
        Throwable thrown = catchThrowable(() -> authenticator.exchangeAuthorizationCode(null));

        // then
        assertThat(thrown).isExactlyInstanceOf(BankServiceException.class);
    }

    @Test
    public void shouldAbortAuthenticationDueToIncorrectChallengeId() {
        // given
        OAuth2Token token = OAuth2Token.create("type", "accessToken", "refreshToken", 500);
        String clientId = "1234";
        HttpResponseException exception = createErrorResponse(403, "API_00006");
        AccountsResponse accounts = getAccountsResponse();

        when(userAvailability.isUserAvailableForInteraction()).thenReturn(true);
        when(abancaConfiguration.getClientId()).thenReturn(clientId);
        when(apiClient.getToken(any())).thenReturn(token);
        when(apiClient.fetchAccounts()).thenReturn(accounts);
        when(apiClient.fetchBalance(any())).thenThrow(exception);

        // when
        Throwable thrown = catchThrowable(() -> authenticator.exchangeAuthorizationCode(null));

        // then
        assertThat(thrown).isExactlyInstanceOf(LoginException.class);
    }

    @Test
    public void shouldAbortAuthenticationDueToUnexpectedErrorCode() {
        // given
        OAuth2Token token = OAuth2Token.create("type", "accessToken", "refreshToken", 500);
        String clientId = "1234";
        HttpResponseException exception = createErrorResponse(409, "API_00011");
        AccountsResponse accounts = getAccountsResponse();

        when(userAvailability.isUserAvailableForInteraction()).thenReturn(true);
        when(abancaConfiguration.getClientId()).thenReturn(clientId);
        when(apiClient.getToken(any())).thenReturn(token);
        when(apiClient.fetchAccounts()).thenReturn(accounts);
        when(apiClient.fetchBalance(any())).thenThrow(exception);

        // when
        Throwable thrown = catchThrowable(() -> authenticator.exchangeAuthorizationCode(null));

        // then
        assertThat(thrown).isExactlyInstanceOf(HttpResponseException.class);
    }

    private HttpResponseException createErrorResponse(int status, String errorCode) {
        HttpResponse mocked = mock(HttpResponse.class);
        when(mocked.getStatus()).thenReturn(status);
        HttpResponseException exception = new HttpResponseException(null, mocked);
        when(exception.getResponse().getBody(ErrorResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                getUnsuccessfulResponse(errorCode), ErrorResponse.class));

        return exception;
    }

    private String getUnsuccessfulResponse(String errorCode) {
        return "{\n"
                + "    \"errors\": [\n"
                + "        {\n"
                + "            \"id\": \"1\",\n"
                + "            \"code\": \""
                + errorCode
                + "\",\n"
                + "            \"title\": \"User should solve a security challenge to access the resource\",\n"
                + "            \"technicalDescription\": \"The resource has a further security requirement that can be fulfilled by resolving a security challenge\",\n"
                + "            \"details\": {\n"
                + "                \"solutionHint\": \"1\",\n"
                + "                \"challengeId\": \"Incorrect\",\n"
                + "                \"attemptsLeft\": 3,\n"
                + "                \"type\": \"OTP\",\n"
                + "                \"responseMode\": \"Header\",\n"
                + "                \"expiresIn\": 598\n"
                + "            }\n"
                + "        }\n"
                + "    ]\n"
                + "}";
    }

    private AccountsResponse getAccountsResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"data\": [\n"
                        + "        {\n"
                        + "            \"relationships\": {\n"
                        + "                \"transactions\": {\n"
                        + "                    \"links\": {\n"
                        + "                        \"related\": \"/psd2/me/accounts/1/transactions\"\n"
                        + "                    }\n"
                        + "                },\n"
                        + "                \"transfers\": {\n"
                        + "                    \"links\": {\n"
                        + "                        \"related\": \"/psd2/me/accounts/1/transfers\"\n"
                        + "                    }\n"
                        + "                },\n"
                        + "                \"fundsAvailability\": {\n"
                        + "                    \"links\": {\n"
                        + "                        \"related\": \"/psd2/me/accounts/1/fundsAvailability\"\n"
                        + "                    }\n"
                        + "                },\n"
                        + "                \"balance\": {\n"
                        + "                    \"links\": {\n"
                        + "                        \"related\": \"/psd2/me/accounts/1/balance\"\n"
                        + "                    }\n"
                        + "                }\n"
                        + "            },\n"
                        + "            \"type\": \"accountMeResponse\",\n"
                        + "            \"id\": \"1\",\n"
                        + "            \"attributes\": {\n"
                        + "                \"identifier\": {\n"
                        + "                    \"number\": \"ES0000000000000000000000\",\n"
                        + "                    \"type\": \"IBAN\"\n"
                        + "                },\n"
                        + "                \"type\": \"C000\"\n"
                        + "            },\n"
                        + "            \"links\": {\n"
                        + "                \"self\": \"/psd2/me/accounts/1\"\n"
                        + "            }\n"
                        + "        }\n"
                        + "    ],\n"
                        + "    \"links\": {\n"
                        + "        \"self\": \"/psd2/me/accounts\"\n"
                        + "    }\n"
                        + "}",
                AccountsResponse.class);
    }
}
