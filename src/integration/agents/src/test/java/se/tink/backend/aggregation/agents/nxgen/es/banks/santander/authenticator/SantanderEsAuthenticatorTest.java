package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.authenticator;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.rpc.LoginResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SantanderEsAuthenticatorTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/santander/resources";

    private static SantanderEsApiClient apiClient;
    private static HttpResponseException httpResponseException;
    private static HttpResponse httpResponse;
    private SantanderEsSessionStorage santanderEsSessionStorage;

    @BeforeClass
    public static void setUpOnce() {
        httpResponseException = mock(HttpResponseException.class);
        httpResponse = mock(HttpResponse.class);
    }

    @Before
    public void setUp() {
        apiClient = mock(SantanderEsApiClient.class);
        santanderEsSessionStorage = new SantanderEsSessionStorage(new SessionStorage());
    }

    @Test
    public void authenticateShouldWorkAsExpected() throws IOException {
        // given
        when(apiClient.authenticateCredentials(any(), any()))
                .thenReturn(
                        new String(
                                Files.readAllBytes(
                                        Paths.get(
                                                TEST_DATA_PATH,
                                                "authenticateCredentialsResponse.xml"))));
        when(apiClient.login())
                .thenReturn(
                        new String(
                                Files.readAllBytes(
                                        Paths.get(
                                                TEST_DATA_PATH,
                                                "authenticatorLoginResponse.xml"))));
        SantanderEsAuthenticator authenticator =
                new SantanderEsAuthenticator(apiClient, santanderEsSessionStorage);

        // when
        authenticator.authenticate("correctUsername", "correctPassword");

        // then
        LoginResponse response = santanderEsSessionStorage.getLoginResponse();
        Assert.assertEquals(1, response.getAccountList().size());
        Assert.assertEquals(3, response.getCards().size());
        Assert.assertEquals("JOHN", response.getNameWithoutSurname().trim());
    }

    @Test(expected = LoginException.class)
    public void authenticateShouldThrowIncorrectCredentialException() throws IOException {
        // given
        when(apiClient.authenticateCredentials(any(), any())).thenThrow(httpResponseException);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getBody(any()))
                .thenReturn(
                        new String(
                                Files.readAllBytes(
                                        Paths.get(
                                                TEST_DATA_PATH,
                                                "incorrectCredentialsResponseError.xml"))));

        SantanderEsAuthenticator authenticator =
                new SantanderEsAuthenticator(apiClient, santanderEsSessionStorage);

        // when
        authenticator.authenticate("incorrect", "incorrect");
    }

    @Test(expected = AuthorizationException.class)
    public void authenticateShouldThrowBlockedCredentialException() throws IOException {
        // given
        when(apiClient.authenticateCredentials(any(), any())).thenThrow(httpResponseException);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getBody(any()))
                .thenReturn(
                        new String(
                                Files.readAllBytes(
                                        Paths.get(
                                                TEST_DATA_PATH,
                                                "blockedCredentialsResponseError.xml"))));
        SantanderEsAuthenticator authenticator =
                new SantanderEsAuthenticator(apiClient, santanderEsSessionStorage);

        // when
        authenticator.authenticate("blockedUser", "mockedpassword");
    }

    @Test(expected = HttpResponseException.class)
    public void authenticateShouldThrowHttpResponseExceptionWhenUnexpectedErrorHappens()
            throws IOException {
        // given
        when(apiClient.authenticateCredentials(any(), any())).thenThrow(httpResponseException);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getBody(any()))
                .thenReturn(
                        new String(
                                Files.readAllBytes(
                                        Paths.get(TEST_DATA_PATH, "unexpectedErrorResponse.xml"))));
        SantanderEsAuthenticator authenticator =
                new SantanderEsAuthenticator(apiClient, santanderEsSessionStorage);

        // when
        authenticator.authenticate("mock1", "mock1password");
    }

    @Test(expected = LoginException.class)
    public void authenticateShouldThrowLoginException() throws IOException {
        // given
        when(apiClient.authenticateCredentials(any(), any()))
                .thenReturn(
                        new String(
                                Files.readAllBytes(
                                        Paths.get(
                                                TEST_DATA_PATH,
                                                "authenticateCredentialsResponse.xml"))));
        when(apiClient.login()).thenThrow(httpResponseException);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getBody(any()))
                .thenReturn(
                        new String(
                                Files.readAllBytes(
                                        Paths.get(
                                                TEST_DATA_PATH,
                                                "authenticatorLoginExceptionResponse.xml"))));
        SantanderEsAuthenticator authenticator =
                new SantanderEsAuthenticator(apiClient, santanderEsSessionStorage);

        // when
        authenticator.authenticate("mocked", "mocked");
    }
}
