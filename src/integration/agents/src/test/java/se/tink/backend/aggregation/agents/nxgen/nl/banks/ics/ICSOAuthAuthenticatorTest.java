package se.tink.backend.aggregation.agents.nxgen.nl.banks.ics;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.ErrorMessages.CONSENT_ERROR;
import static se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.ErrorMessages.INVALID_TOKEN;

import agents_platform_agents_framework.org.springframework.test.util.ReflectionTestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.ICSOAuthAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.error.exceptions.UnexpectedErrorException;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc.AccountSetupResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc.ClientCredentialTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc.ErrorBody;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class ICSOAuthAuthenticatorTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ICSOAuthAuthenticator icsoAuthAuthenticator;
    private ICSApiClient client;

    private SessionStorage sessionStorage;
    private HttpResponseException exception;

    @Before
    public void setUp() throws Exception {
        client = mock(ICSApiClient.class);
        sessionStorage = new SessionStorage();
        exception = mock(HttpResponseException.class, RETURNS_DEEP_STUBS);
        PersistentStorage persistentStorage = mock(PersistentStorage.class);
        icsoAuthAuthenticator =
                new ICSOAuthAuthenticator(client, sessionStorage, persistentStorage);
    }

    @Test
    public void shouldBuildAuthorizeUrlProperly() {
        // given
        ClientCredentialTokenResponse clientCredentialTokenResponse =
                mock(ClientCredentialTokenResponse.class);

        URL expectedUrl = new URL("sth");
        AccountSetupResponse accountSetupResponse = TestHelper.getAccountSetupResponse();

        final OAuth2Token token = mock(OAuth2Token.class);
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);

        // when
        when(client.fetchTokenWithClientCredential()).thenReturn(clientCredentialTokenResponse);
        when(clientCredentialTokenResponse.toTinkToken()).thenReturn(token);
        when(client.setupAccount(token)).thenReturn(accountSetupResponse);
        when(client.createAuthorizeRequest(anyString(), anyString()))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.getUrl()).thenReturn(expectedUrl);

        URL url = icsoAuthAuthenticator.buildAuthorizeUrl(TestHelper.STATE);

        // then
        assertNotNull(url);
        assertEquals(expectedUrl, url);
    }

    @Test
    public void shouldThrowExceptionNotReceivePermissionsWhereResponseIsFalse() {
        // given
        ClientCredentialTokenResponse clientCredentialTokenResponse =
                mock(ClientCredentialTokenResponse.class);

        // when
        when(client.fetchTokenWithClientCredential()).thenReturn(clientCredentialTokenResponse);

        // then
        assertThatThrownBy(() -> icsoAuthAuthenticator.buildAuthorizeUrl(TestHelper.STATE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(ErrorMessages.MISSING_PERMISSIONS);
    }

    @Test
    public void shouldRefreshAccessToken() {
        // given
        OAuth2Token expected =
                OAuth2Token.create(
                        "refresh", TestHelper.ACCESS_TOKEN, TestHelper.REFRESH_TOKEN, 600);

        // when
        when(client.refreshToken(TestHelper.REFRESH_TOKEN)).thenReturn(expected);

        // then
        assertEquals(expected, icsoAuthAuthenticator.refreshAccessToken(TestHelper.REFRESH_TOKEN));
    }

    @Test
    public void shouldExpireSessionWhenInvalidToken() {
        // given
        ErrorBody errorResponse =
                OBJECT_MAPPER.convertValue(TestHelper.getError(INVALID_TOKEN), ErrorBody.class);

        // when
        when(exception.getResponse().getBody(ErrorBody.class)).thenReturn(errorResponse);
        when(exception.getResponse().getStatus()).thenReturn(401);
        when(client.refreshToken(TestHelper.REFRESH_TOKEN)).thenThrow(exception);

        // then
        assertThatThrownBy(() -> icsoAuthAuthenticator.refreshAccessToken(TestHelper.REFRESH_TOKEN))
                .isInstanceOf(SessionException.class);
    }

    @Test
    public void shouldExpireSessionWhenAccessIsForbidden() {
        // given
        ErrorBody errorResponse =
                OBJECT_MAPPER.convertValue(TestHelper.getError(CONSENT_ERROR), ErrorBody.class);

        // when
        when(exception.getResponse().getBody(ErrorBody.class)).thenReturn(errorResponse);
        when(exception.getResponse().getStatus()).thenReturn(403);
        when(client.refreshToken(TestHelper.REFRESH_TOKEN)).thenThrow(exception);

        // then
        assertThatThrownBy(() -> icsoAuthAuthenticator.refreshAccessToken(TestHelper.REFRESH_TOKEN))
                .isInstanceOf(SessionException.class);
    }

    @Test
    public void shouldThrowUnexpectedExceptionWhenErrorIsNotUnauthorizedOrForbidden() {
        // given
        ErrorBody errorResponse =
                OBJECT_MAPPER.convertValue(TestHelper.getError(CONSENT_ERROR), ErrorBody.class);

        // when
        when(exception.getResponse().getBody(ErrorBody.class)).thenReturn(errorResponse);
        when(exception.getResponse().getStatus()).thenReturn(500);
        when(client.refreshToken(TestHelper.REFRESH_TOKEN)).thenThrow(exception);

        // then
        assertThatThrownBy(() -> icsoAuthAuthenticator.refreshAccessToken(TestHelper.REFRESH_TOKEN))
                .isInstanceOf(UnexpectedErrorException.class)
                .hasMessage(errorResponse.getError());
    }

    @Test
    public void shouldReturnTokenWithoutCheckingTokenExpirationAsNotRefreshNotPresent() {
        // given
        OAuth2Token token = OAuth2Token.create("bearer", TestHelper.ACCESS_TOKEN, null, 600);

        // then
        assertEquals(
                token,
                ReflectionTestUtils.invokeMethod(
                        icsoAuthAuthenticator, "addRefreshExpireToToken", token));
    }

    @Test
    public void shouldSetNewExpirationTimeForToken() {
        // given
        OAuth2Token token =
                OAuth2Token.create(
                        "bearer", TestHelper.ACCESS_TOKEN, TestHelper.REFRESH_TOKEN, 600);

        long refreshExpiresInSeconds = 8365879862L;
        OAuth2Token newToken =
                OAuth2Token.create(
                        "bearer",
                        TestHelper.ACCESS_TOKEN,
                        TestHelper.REFRESH_TOKEN,
                        600,
                        refreshExpiresInSeconds);

        Date expirationDate = new Date();
        expirationDate.setTime(9999999999999L);
        sessionStorage.put(StorageKeys.EXPIRATION_DATE, expirationDate);

        // when
        OAuth2Token result =
                ReflectionTestUtils.invokeMethod(
                        icsoAuthAuthenticator, "addRefreshExpireToToken", token);

        // then
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("refreshExpiresInSeconds")
                .isEqualTo(newToken);
        assert result != null;
        assert result.getRefreshExpiresInSeconds() >= 83658798L;
    }
}
