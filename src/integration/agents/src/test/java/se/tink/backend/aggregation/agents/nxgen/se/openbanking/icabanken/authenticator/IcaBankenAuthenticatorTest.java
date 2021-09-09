package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import agents_platform_agents_framework.org.springframework.test.util.ReflectionTestUtils;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.ErrorTypes;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.ProductionUrls;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.AccountsErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.TokenErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.configuration.IcaBankenConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.CallbackParams;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(JUnitParamsRunner.class)
public class IcaBankenAuthenticatorTest {

    private PersistentStorage persistentStorage;
    private TinkHttpClient client;
    private IcaBankenAuthenticator icaBankenAuthenticator;
    private static final String QWAC =
            "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";
    private final String token = "000000000000000000000000YWY4LWEyMDItM2JlYjVlM2U2Mjc2";
    private HttpResponse httpResponse;
    private HttpResponseException exception;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        persistentStorage = mock(PersistentStorage.class);
        AgentConfiguration<IcaBankenConfiguration> agentConfiguration =
                mock(AgentConfiguration.class);
        Credentials credentials = mock(Credentials.class);
        client = mock(TinkHttpClient.class);
        Mockito.when(agentConfiguration.getQwac()).thenReturn(QWAC);
        IcaBankenApiClient apiClient = new IcaBankenApiClient(client, persistentStorage);
        icaBankenAuthenticator =
                new IcaBankenAuthenticator(
                        apiClient, persistentStorage, agentConfiguration, credentials);
        HttpRequest httpRequest = mock(HttpRequest.class);
        httpResponse = mock(HttpResponse.class);
        exception = new HttpResponseException(httpRequest, httpResponse);
    }

    @Test
    public void shouldReturnValidRefreshAccessTokenResponse() {
        // given
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        TokenResponse tokenResponse = mock(TokenResponse.class);
        OAuth2Token oAuth2Token =
                OAuth2Token.create("bearer", "AccessToken123", "RefreshToken123", 500);

        // when
        Mockito.when(client.request(new URL(ProductionUrls.TOKEN_PATH))).thenReturn(requestBuilder);
        Mockito.when(
                        requestBuilder.body(
                                Mockito.any(), Mockito.eq(MediaType.APPLICATION_FORM_URLENCODED)))
                .thenReturn(requestBuilder);
        Mockito.when(requestBuilder.post(TokenResponse.class)).thenReturn(tokenResponse);
        Mockito.when(tokenResponse.toOauthToken()).thenReturn(oAuth2Token);
        OAuth2Token oAuth2TokenResult = icaBankenAuthenticator.refreshAccessToken(token);

        // then
        Assert.assertNotNull(oAuth2TokenResult);
        Assert.assertEquals(oAuth2Token, oAuth2TokenResult);
    }

    @Test
    public void shouldThrowHttpResponseExceptionWhenRefreshingTokenGetsScBadRequest() {
        // when
        Mockito.when(client.request(new URL(ProductionUrls.TOKEN_PATH))).thenThrow(exception);
        Mockito.when(httpResponse.getBody(TokenErrorResponse.class))
                .thenReturn(mock(TokenErrorResponse.class));
        Mockito.when(httpResponse.getStatus()).thenReturn(HttpStatus.SC_BAD_REQUEST);

        // then
        assertThatThrownBy(() -> icaBankenAuthenticator.refreshAccessToken(token))
                .isInstanceOf(HttpResponseException.class);
    }

    @Test
    public void shouldThrowBankServiceExceptionWhenRefreshingTokenBankSideFailureError() {
        // given
        TokenErrorResponse errorResponse = mock(TokenErrorResponse.class);

        // when
        Mockito.when(client.request(new URL(ProductionUrls.TOKEN_PATH))).thenThrow(exception);
        Mockito.when(httpResponse.getStatus()).thenReturn(HttpStatus.SC_BAD_REQUEST);
        Mockito.when(httpResponse.getBody(TokenErrorResponse.class)).thenReturn(errorResponse);
        Mockito.when(errorResponse.isInternalServerError()).thenReturn(true);

        // then
        assertThatThrownBy(() -> icaBankenAuthenticator.refreshAccessToken(token))
                .isInstanceOf(BankServiceError.BANK_SIDE_FAILURE.exception().getClass());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnVoidWhenErrorTypeIsNotUnknown() {
        // given
        Map<String, String> callbackData = mock(Map.class);

        // when
        Mockito.when(callbackData.getOrDefault(CallbackParams.ERROR, null))
                .thenReturn(ErrorTypes.SERVER_ERROR);
        icaBankenAuthenticator.handleSpecificCallbackDataError(callbackData);

        // then
        verify(callbackData, times(1)).getOrDefault(Mockito.anyString(), Mockito.any());
    }

    @Test
    @Parameters(method = "bankServiceErrorDescription")
    public void shouldThrowException(String errorMessage, AgentError error) {
        // given
        Map<String, String> callbackData = new HashMap<>();
        callbackData.put(CallbackParams.ERROR, ErrorTypes.UNKNOWN);
        callbackData.put(CallbackParams.ERROR_DESCRIPTION, errorMessage);

        // then
        assertThatThrownBy(
                        () -> icaBankenAuthenticator.handleSpecificCallbackDataError(callbackData))
                .isInstanceOf(error.exception().getClass());
    }

    private Object[] bankServiceErrorDescription() {
        return new Object[] {
            new Object[] {ErrorMessages.CANCEL, ThirdPartyAppError.CANCELLED},
            new Object[] {ErrorMessages.START_FAILED, ThirdPartyAppError.TIMED_OUT},
            new Object[] {
                ErrorMessages.UNEXPECTED_INTERNAL_EXCEPTION, BankServiceError.BANK_SIDE_FAILURE
            },
            new Object[] {ErrorMessages.INTERNAL_SERVER_ERROR, BankServiceError.BANK_SIDE_FAILURE}
        };
    }

    @Test
    public void shouldVerifyValidCustomerStatus() {
        // given
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        FetchAccountsResponse accountsResponse = mock(FetchAccountsResponse.class);
        OAuth2Token oAuth2Token = mock(OAuth2Token.class);

        // when
        Mockito.when(client.request(new URL(ProductionUrls.ACCOUNTS_PATH)))
                .thenReturn(requestBuilder);
        Mockito.when(requestBuilder.queryParam(Mockito.any(), Mockito.any()))
                .thenReturn(requestBuilder);
        Mockito.when(requestBuilder.header(Mockito.any(), Mockito.any()))
                .thenReturn(requestBuilder, requestBuilder);
        Mockito.when(requestBuilder.addBearerToken(Mockito.any())).thenReturn(requestBuilder);
        Mockito.when(requestBuilder.get(FetchAccountsResponse.class)).thenReturn(accountsResponse);
        Mockito.when(persistentStorage.get(StorageKeys.TOKEN, OAuth2Token.class))
                .thenReturn(java.util.Optional.ofNullable(oAuth2Token));
        ReflectionTestUtils.invokeMethod(
                icaBankenAuthenticator, "verifyValidCustomerStatusOrThrow");

        // then
        verify(client, times(1)).request(new URL(ProductionUrls.ACCOUNTS_PATH));
    }

    @Test
    public void shouldThrowHttpResponseExceptionWhenResponseStatusIsScConflict() {
        // when
        Mockito.when(client.request(new URL(ProductionUrls.ACCOUNTS_PATH))).thenThrow(exception);
        Mockito.when(httpResponse.getStatus()).thenReturn(HttpStatus.SC_CONFLICT);

        // then
        assertThatThrownBy(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        icaBankenAuthenticator, "verifyValidCustomerStatusOrThrow"))
                .isInstanceOf(HttpResponseException.class);
    }

    @Test
    public void shouldThrowNoAccessToMobileBankingExceptionWhenOldKycInfoError() {
        // given
        AccountsErrorResponse errorResponse = mock(AccountsErrorResponse.class);

        // when
        Mockito.when(client.request(new URL(ProductionUrls.ACCOUNTS_PATH))).thenThrow(exception);
        Mockito.when(httpResponse.getStatus()).thenReturn(HttpStatus.SC_FORBIDDEN);
        Mockito.when(httpResponse.getBody(AccountsErrorResponse.class)).thenReturn(errorResponse);
        Mockito.when(errorResponse.isOldKycInfoError()).thenReturn(true);

        // then
        assertThatThrownBy(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        icaBankenAuthenticator, "verifyValidCustomerStatusOrThrow"))
                .isInstanceOf(LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception().getClass());
        Assert.assertEquals(
                "You do not have access to mobile banking. Please contact your bank.",
                LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception().getError().userMessage().get());
    }

    @Test
    public void shouldThrowNotCustomerExceptionWhenNoAccountInformation() {
        // given
        AccountsErrorResponse errorResponse = mock(AccountsErrorResponse.class);

        // when
        Mockito.when(client.request(new URL(ProductionUrls.ACCOUNTS_PATH))).thenThrow(exception);
        Mockito.when(httpResponse.getStatus()).thenReturn(HttpStatus.SC_FORBIDDEN);
        Mockito.when(httpResponse.getBody(AccountsErrorResponse.class)).thenReturn(errorResponse);
        Mockito.when(errorResponse.isNoAccountInformation()).thenReturn(true);

        // then
        assertThatThrownBy(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        icaBankenAuthenticator, "verifyValidCustomerStatusOrThrow"))
                .isInstanceOf(LoginError.NOT_CUSTOMER.exception().getClass());
        Assert.assertEquals(
                "You don't have any commitments in the selected bank.",
                LoginError.NOT_CUSTOMER.exception().getError().userMessage().get());
    }

    @Test
    public void shouldThrowHttpResponseException() {
        // given
        AccountsErrorResponse errorResponse = mock(AccountsErrorResponse.class);

        // when
        Mockito.when(client.request(new URL(ProductionUrls.ACCOUNTS_PATH))).thenThrow(exception);
        Mockito.when(httpResponse.getStatus()).thenReturn(HttpStatus.SC_FORBIDDEN);
        Mockito.when(httpResponse.getBody(AccountsErrorResponse.class)).thenReturn(errorResponse);

        // then
        assertThatThrownBy(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        icaBankenAuthenticator, "verifyValidCustomerStatusOrThrow"))
                .isInstanceOf(HttpResponseException.class);
    }
}
