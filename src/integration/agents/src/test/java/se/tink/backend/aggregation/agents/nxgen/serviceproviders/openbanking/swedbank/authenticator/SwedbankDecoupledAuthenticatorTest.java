package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import agents_platform_agents_framework.org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.consent.SwedbankConsentHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.resources.GenericResponseTestData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.rpc.GenericResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

@RunWith(MockitoJUnitRunner.class)
public class SwedbankDecoupledAuthenticatorTest {
    private SwedbankApiClient apiClient;
    private SupplementalInformationHelper supplementalInformationHelper;
    private PersistentStorage persistentStorage;
    private SwedbankConsentHandler consentHandler;
    private SwedbankDecoupledAuthenticator objectUnderTest;
    private AgentComponentProvider componentProvider;
    private CredentialsRequest credentialsRequest;
    @Mock private HttpResponse httpResponse;
    @Mock private HttpResponseException httpResponseException;

    @Before
    public void setUp() {
        persistentStorage = new PersistentStorage();
        apiClient = mock(SwedbankApiClient.class);
        supplementalInformationHelper = mock(SupplementalInformationHelper.class);
        consentHandler = mock(SwedbankConsentHandler.class);
        componentProvider = mock(AgentComponentProvider.class);
        credentialsRequest = mock(CredentialsRequest.class);

        objectUnderTest =
                new SwedbankDecoupledAuthenticator(
                        apiClient,
                        supplementalInformationHelper,
                        persistentStorage,
                        consentHandler,
                        componentProvider);
    }

    @Test
    public void shouldReturnBankIdStatusDoneWhenAuthenticationIsFinalised() {
        // given
        when(apiClient.collectAuthStatus(any(), any()))
                .thenReturn(SwedbankDecoupledAuthenticatorTestData.AUTH_FINALISED_RESPONSE);
        when(apiClient.exchangeCodeForToken("mockedAuthorizationCode"))
                .thenReturn(SwedbankDecoupledAuthenticatorTestData.TOKEN_RESPONSE.toTinkToken());
        when(apiClient.isSwedbank()).thenReturn(true);
        when(componentProvider.getCredentialsRequest()).thenReturn(credentialsRequest);

        // when
        final BankIdStatus bankIdStatus = objectUnderTest.collect("collectAuthUri");

        // then
        assertThat(bankIdStatus).isEqualTo(BankIdStatus.DONE);
    }

    // multiple engagements
    @Test
    public void shouldRetryWithSwedbankIfMultipleEngagementsAndProviderIsSwedbank() {
        // given
        when(httpResponse.getBody(GenericResponse.class))
                .thenReturn(GenericResponseTestData.MISSING_BANK_ID);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(apiClient.collectAuthStatus(any(), any())).thenThrow(httpResponseException);

        when(apiClient.isSwedbank()).thenReturn(true);

        when(apiClient.supplyBankId(any(), any(), any()))
                .thenReturn(SwedbankDecoupledAuthenticatorTestData.AUTH_STARTED_RESPONSE);

        // when
        final BankIdStatus bankIdStatus = objectUnderTest.collect("collectAuthUri");

        // then
        verify(apiClient).supplyBankId(any(), any(), eq("08999"));
        assertThat(bankIdStatus).isEqualTo(BankIdStatus.WAITING);
    }

    @Test
    public void shouldRetryWithSavingsbankChoiceIfMultipleEngagementsAndProviderIsNotSwedbank() {
        // given
        when(httpResponse.getBody(GenericResponse.class))
                .thenReturn(GenericResponseTestData.MISSING_BANK_ID);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(apiClient.collectAuthStatus(any(), any())).thenThrow(httpResponseException);

        when(apiClient.isSwedbank()).thenReturn(false);

        // 1 corresponds to bank ID 08191
        when(supplementalInformationHelper.waitForLoginInput()).thenReturn("1");

        when(apiClient.supplyBankId(any(), any(), any()))
                .thenReturn(SwedbankDecoupledAuthenticatorTestData.AUTH_STARTED_RESPONSE);

        // when
        final BankIdStatus bankIdStatus = objectUnderTest.collect("collectAuthUri");

        // then
        verify(apiClient).supplyBankId(any(), any(), eq("08191"));
        assertThat(bankIdStatus).isEqualTo(BankIdStatus.WAITING);
    }

    @Test
    public void shouldReturnBankIdStatusCancelledWhenUserCancelsBankId() {
        // given
        when(apiClient.collectAuthStatus(any(), any()))
                .thenReturn(SwedbankDecoupledAuthenticatorTestData.BANK_ID_CANCEL_RESPONSE);

        // when
        final BankIdStatus bankIdStatus = objectUnderTest.collect("collectAuthUri");

        // then
        assertThat(bankIdStatus).isEqualTo(BankIdStatus.CANCELLED);
    }

    @Test
    public void shouldReturnBankIdStatusExpiredAutoStartTokenOnAuthStatusFailed() {
        // given
        when(apiClient.collectAuthStatus(any(), any()))
                .thenReturn(SwedbankDecoupledAuthenticatorTestData.AUTH_FAILED_RESPONSE);

        // when
        final BankIdStatus bankIdStatus = objectUnderTest.collect("collectAuthUri");

        // then
        assertThat(bankIdStatus).isEqualTo(BankIdStatus.EXPIRED_AUTOSTART_TOKEN);
    }

    @Test
    public void shouldReturnBankIdStatusFailedUnknownOnUnknownStatus() {
        // given
        when(apiClient.collectAuthStatus(any(), any()))
                .thenReturn(SwedbankDecoupledAuthenticatorTestData.UNKNOWN_AUTH_STATUS_RESPONSE);

        // when
        final BankIdStatus bankIdStatus = objectUnderTest.collect("collectAuthUri");

        // then
        assertThat(bankIdStatus).isEqualTo(BankIdStatus.FAILED_UNKNOWN);
    }

    @Test
    public void shouldCompleteAuthenticationIfUserHasNotCrossedLoggedIn() {
        // given
        when(apiClient.isSwedbank()).thenReturn(false);
        when(apiClient.fetchAccounts())
                .thenReturn(SwedbankDecoupledAuthenticatorTestData.SAVINGSBANK_ACCOUNTS_RESPONSE);
        when(componentProvider.getCredentialsRequest()).thenReturn(credentialsRequest);

        // when
        final Throwable thrown =
                catchThrowable(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        objectUnderTest, "completeAuthentication"));

        // then
        assertThat(thrown).isNull();
    }

    /**
     * This scenario occurs if user has a single engagement at Swedbank and selects Savingsbank
     * provider by mistake.
     */
    @Test
    public void shouldThrowLoginErrorIfUserHasCrossedLogin() {
        // given
        when(apiClient.isSwedbank()).thenReturn(false);
        when(apiClient.fetchAccounts())
                .thenReturn(SwedbankDecoupledAuthenticatorTestData.SWEDBANK_ACCOUNTS_RESPONSE);
        when(componentProvider.getCredentialsRequest()).thenReturn(credentialsRequest);

        // when
        final ThrowingCallable callable =
                () -> ReflectionTestUtils.invokeMethod(objectUnderTest, "completeAuthentication");

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.NOT_CUSTOMER");
    }

    @Test
    public void shouldReturnAccessTokenOnSuccessfulRefreshAccessToken() {
        // given
        final OAuth2Token oAuthTokenFromBank =
                SwedbankDecoupledAuthenticatorTestData.TOKEN_RESPONSE.toTinkToken();
        when(apiClient.refreshToken(any())).thenReturn(oAuthTokenFromBank);

        // when
        final Optional<OAuth2Token> oAuth2Token = objectUnderTest.refreshAccessToken(any());

        // then
        assertThat(oAuth2Token).isPresent();
        assertThat(oAuth2Token.get()).isEqualTo(oAuthTokenFromBank);
        assertNotNull(persistentStorage.get(PersistentStorageKeys.OAUTH_2_TOKEN));
    }

    @Test
    public void shouldThrowSessionErrorIfRefreshTokenHasExpired() {
        // given
        when(httpResponse.getStatus()).thenReturn(400);
        when(httpResponse.getBody(GenericResponse.class))
                .thenReturn(GenericResponseTestData.TOKEN_EXPIRED);
        when(httpResponseException.getResponse()).thenReturn(httpResponse);
        when(apiClient.refreshToken(any())).thenThrow(httpResponseException);

        // when
        final ThrowingCallable callable = () -> objectUnderTest.refreshAccessToken(any());

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
    }

    @Test
    public void shouldThrowBankIdErrorIfBankIdIsAlreadyInProgress() {
        // when
        final ThrowingCallable callable =
                () ->
                        ReflectionTestUtils.invokeMethod(
                                objectUnderTest,
                                "handleBankIdError",
                                GenericResponseTestData.BANKID_ALREADY_IN_PROGRESS);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(BankIdException.class)
                .hasMessage("Cause: BankIdError.ALREADY_IN_PROGRESS");
    }

    @Test
    public void shouldThrowAuthorizationErrorIfKycErrorIsReceived() {
        // when
        final ThrowingCallable callable =
                () ->
                        ReflectionTestUtils.invokeMethod(
                                objectUnderTest,
                                "handleBankIdError",
                                GenericResponseTestData.INVALID_KYC);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("Cause: AuthorizationError.ACCOUNT_BLOCKED");
    }

    @Test
    public void shouldThrowLoginErrorIfMissingBankAgreement() {
        // when
        final ThrowingCallable callable =
                () ->
                        ReflectionTestUtils.invokeMethod(
                                objectUnderTest,
                                "handleBankIdError",
                                GenericResponseTestData.INTERNET_BANK_AGREEMENT);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.NOT_CUSTOMER");
    }

    @Test
    public void shouldThrowLoginErrorIfNoProfileAvailableAndProviderIsSwedbank() {
        // given
        when(apiClient.isSwedbank()).thenReturn(true);

        // when
        final ThrowingCallable callable =
                () ->
                        ReflectionTestUtils.invokeMethod(
                                objectUnderTest,
                                "handleBankIdError",
                                GenericResponseTestData.NO_PROFILE);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.NOT_CUSTOMER");
    }

    @Test
    public void shouldThrowLoginErrorIfNoProfileAvailableAndProviderIsNotSwedbank() {
        // given (Savingsbank provider)
        when(apiClient.isSwedbank()).thenReturn(false);

        // when
        final ThrowingCallable callable =
                () ->
                        ReflectionTestUtils.invokeMethod(
                                objectUnderTest,
                                "handleBankIdError",
                                GenericResponseTestData.NO_PROFILE);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.NOT_CUSTOMER");
    }

    @Test
    public void shouldThrowLoginErrorIncorrectCredentialsIfWrongUserId() {
        // when
        final ThrowingCallable callable =
                () ->
                        ReflectionTestUtils.invokeMethod(
                                objectUnderTest,
                                "handleBankIdError",
                                GenericResponseTestData.WRONG_USER_ID);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void shouldThrowBankIdErrorTimeoutIfAuthenticationHasExpired() {
        // when
        final ThrowingCallable callable =
                () ->
                        ReflectionTestUtils.invokeMethod(
                                objectUnderTest,
                                "handleBankIdError",
                                GenericResponseTestData.AUTHORIZATION_EXPIRED);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(BankIdException.class)
                .hasMessage("Cause: BankIdError.TIMEOUT");
    }
}
