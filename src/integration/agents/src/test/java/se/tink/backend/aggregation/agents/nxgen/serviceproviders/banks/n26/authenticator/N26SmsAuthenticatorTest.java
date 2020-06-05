package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.entities.TokenEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.sms.MultiFactorSmsResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class N26SmsAuthenticatorTest {

    private static final String USERNAME = "test_username";
    private static final String PASSWORD = "test_password";
    private static final String SMS_OTP = "test_otp";
    private static final String MFA_TOKEN_STORAGE_KEY = "MFA_TOKEN";
    private static final String TOKEN_ENTITY_STORAGE_KEY = "TOKEN_ENTITY";
    private static final String MFA_TOKEN = "test_mfa_token";

    private SessionStorage sessionStorage;
    private N26SmsAuthenticator authenticator;

    @Before
    public void before() throws LoginException {
        sessionStorage = new SessionStorage();
        N26ApiClient client = mock(N26ApiClient.class);

        when(client.loginWithPassword(any(), any()))
                .thenAnswer(
                        invocation -> {
                            String username = invocation.getArguments()[0].toString();
                            String password = invocation.getArguments()[1].toString();
                            if (!USERNAME.equals(username) || !PASSWORD.equals(password)) {
                                throw LoginError.INCORRECT_CREDENTIALS.exception();
                            }
                            return MFA_TOKEN;
                        });

        when(client.loginWithOtp(anyString()))
                .thenAnswer(
                        invocation -> {
                            String otp = invocation.getArguments()[0].toString();
                            if (!SMS_OTP.equals(otp)) {
                                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
                            }
                            return N26AuthenticatorTestData.getTokenResponse();
                        });

        when(client.initiate2fa(anyString(), eq(MultiFactorSmsResponse.class), anyString()))
                .thenReturn(N26AuthenticatorTestData.getInitSms2faResponse());

        authenticator = new N26SmsAuthenticator(sessionStorage, client);
    }

    @Test
    public void shouldFetchMFATokenForCorrectUsernameAndPassword()
            throws AuthenticationException, AuthorizationException {
        // given
        // when
        authenticator.init(USERNAME, PASSWORD);
        // then
        assertThat(sessionStorage).containsEntry(MFA_TOKEN_STORAGE_KEY, MFA_TOKEN);
    }

    @Test
    public void shouldThrowIncorrectCredentialsExceptionForIncorrectUsername() {
        // given
        ThrowableAssert.ThrowingCallable authenticateCall =
                () -> authenticator.init("xx" + USERNAME, PASSWORD);
        // when then
        assertThatCode(authenticateCall)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void shouldThrowIncorrectCredentialsExceptionForIncorrectPassword() {
        // given
        ThrowableAssert.ThrowingCallable authenticateCall =
                () -> authenticator.init(USERNAME, "xx" + PASSWORD);
        // when then
        assertThatCode(authenticateCall)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void shouldAuthenticateCorrectlyForCorrectSmsOtp()
            throws AuthenticationException, AuthorizationException {
        // given
        String mfaCode = authenticator.init(USERNAME, PASSWORD);
        // when
        authenticator.authenticate(SMS_OTP, "");
        // then
        assertThat(sessionStorage).containsKey(TOKEN_ENTITY_STORAGE_KEY);
        Optional<TokenEntity> maybeTokenEntity =
                sessionStorage.get(TOKEN_ENTITY_STORAGE_KEY, TokenEntity.class);
        assertThat(maybeTokenEntity).isPresent();
        TokenEntity tokenEntity = maybeTokenEntity.get();
        assertThat(tokenEntity)
                .hasFieldOrPropertyWithValue("accessToken", N26AuthenticatorTestData.ACCESS_TOKEN);
        assertThat(tokenEntity)
                .hasFieldOrPropertyWithValue(
                        "refreshToken", N26AuthenticatorTestData.REFRESH_TOKEN);
    }

    @Test
    public void shouldThrowIncorrectChallengeResponseExceptionForIncorrectSmsOtp()
            throws AuthenticationException, AuthorizationException {
        // given
        String mfaCode = authenticator.init(USERNAME, PASSWORD);
        // when (not really)
        ThrowableAssert.ThrowingCallable passOtpCall =
                () -> authenticator.authenticate("xx" + SMS_OTP, "");
        // then
        assertThatCode(passOtpCall)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CHALLENGE_RESPONSE");
    }
}
