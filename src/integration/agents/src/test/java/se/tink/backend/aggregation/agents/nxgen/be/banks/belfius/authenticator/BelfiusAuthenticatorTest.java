package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.SendCardNumberResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.signature.BelfiusSignatureCreator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BelfiusAuthenticatorTest extends BelfiusBaseTest {

    private static final String DEVICE_TOKEN = "token";
    private static final String DEVICE_TOKEN_HASHED = "hashed_token";
    private static final String DEVICE_TOKEN_HASHED_HASHED = "hashed_hashed_token";
    private static final String PAN_NUMBER = "123";
    private static final String PASSWORD = "xyz";
    private static final String CONTRACT_NUMBER = "99";
    private static final String CHALLENGE = "ch";
    private static final String SIGNATURE_SOFT = "signAbcSoft";
    private static final String SIGNATURE_PW = "signXyzPw";

    private BelfiusAuthenticator belfiusAuthenticator;

    private BelfiusApiClient belfiusApiClientMock;

    @Before
    public void setup() throws LoginException {
        final Credentials credentialsMock = createCredentialsMock();
        final PersistentStorage persistentStorageMock = createPersistentStorageMock();
        final BelfiusSessionStorage belfiusSessionStorageMock = mock(BelfiusSessionStorage.class);
        final SupplementalInformationHelper supplementalInformationHelperMock =
                mock(SupplementalInformationHelper.class);
        final BelfiusSignatureCreator belfiusSignatureCreatorMock =
                createBelfiusSignatureCreatorMock();
        final HumanInteractionDelaySimulator humanInteractionDelaySimulatorMock =
                mock(HumanInteractionDelaySimulator.class);

        belfiusApiClientMock = createApiClientMock();

        belfiusAuthenticator =
                new BelfiusAuthenticator(
                        belfiusApiClientMock,
                        credentialsMock,
                        persistentStorageMock,
                        belfiusSessionStorageMock,
                        supplementalInformationHelperMock,
                        belfiusSignatureCreatorMock,
                        humanInteractionDelaySimulatorMock);
    }

    @Test
    public void shouldAuthenticate() {
        // given
        final LoginResponse loginResponse =
                getLoginResponse(BelfiusLoginTestData.GOOD_LOGIN_RESPONSE);
        when(belfiusApiClientMock.login(
                        DEVICE_TOKEN_HASHED, DEVICE_TOKEN_HASHED_HASHED, SIGNATURE_SOFT))
                .thenReturn(loginResponse);
        when(belfiusApiClientMock.loginPw(
                        DEVICE_TOKEN_HASHED, DEVICE_TOKEN_HASHED_HASHED, SIGNATURE_PW))
                .thenReturn(loginResponse);

        // when
        final Throwable thrown =
                catchThrowable(() -> belfiusAuthenticator.authenticate(PAN_NUMBER, PASSWORD));

        // then
        assertThat(thrown).isNull();
    }

    @Test
    public void shouldAuthenticateThrowExceptionWhenIncorrectCredentialsDuringLogin() {
        // given
        final LoginResponse loginResponse =
                getLoginResponse(BelfiusLoginTestData.WRONG_CREDENTIALS);
        when(belfiusApiClientMock.login(
                        DEVICE_TOKEN_HASHED, DEVICE_TOKEN_HASHED_HASHED, SIGNATURE_SOFT))
                .thenReturn(loginResponse);

        // when
        final Throwable thrown =
                catchThrowable(() -> belfiusAuthenticator.authenticate(PAN_NUMBER, PASSWORD));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void shouldAuthenticateThrowExceptionWhenIncorrectCredentialsDuringLoginPw() {
        // given
        final LoginResponse loginResponse =
                getLoginResponse(BelfiusLoginTestData.GOOD_LOGIN_RESPONSE);
        when(belfiusApiClientMock.login(
                        DEVICE_TOKEN_HASHED, DEVICE_TOKEN_HASHED_HASHED, SIGNATURE_SOFT))
                .thenReturn(loginResponse);

        final LoginResponse loginPwResponse =
                getLoginResponse(BelfiusLoginTestData.WRONG_CREDENTIALS);
        when(belfiusApiClientMock.loginPw(
                        DEVICE_TOKEN_HASHED, DEVICE_TOKEN_HASHED_HASHED, SIGNATURE_PW))
                .thenReturn(loginPwResponse);

        // when
        final Throwable thrown =
                catchThrowable(() -> belfiusAuthenticator.authenticate(PAN_NUMBER, PASSWORD));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void shouldAutoAuthenticate() {
        // given
        final LoginResponse loginResponse =
                getLoginResponse(BelfiusLoginTestData.GOOD_LOGIN_RESPONSE);
        when(belfiusApiClientMock.loginPw(
                        DEVICE_TOKEN_HASHED, DEVICE_TOKEN_HASHED_HASHED, SIGNATURE_PW))
                .thenReturn(loginResponse);

        // when
        final Throwable thrown = catchThrowable(belfiusAuthenticator::autoAuthenticate);

        // then
        assertThat(thrown).isNull();
    }

    @Test
    public void shouldAutoAuthenticateThrowExceptionWhenPrepareLoginFails() throws LoginException {
        // given
        when(belfiusApiClientMock.prepareLogin(PAN_NUMBER))
                .thenThrow(LoginError.REGISTER_DEVICE_ERROR.exception());

        // when
        final Throwable thrown = catchThrowable(belfiusAuthenticator::autoAuthenticate);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.REGISTER_DEVICE_ERROR");
    }

    @Test
    public void shouldAutoAuthenticateThrowExceptionWhenIncorrectCredentialsDuringLoginPw() {
        // given
        final LoginResponse loginResponse =
                getLoginResponse(BelfiusLoginTestData.WRONG_CREDENTIALS);
        when(belfiusApiClientMock.loginPw(
                        DEVICE_TOKEN_HASHED, DEVICE_TOKEN_HASHED_HASHED, SIGNATURE_PW))
                .thenReturn(loginResponse);

        // when
        final Throwable thrown = catchThrowable(belfiusAuthenticator::autoAuthenticate);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void shouldAutoAuthenticateThrowExceptionWhenBankReportsExtendedInactivityPeriod() {
        // given
        final LoginResponse loginResponse =
                getLoginResponse(BelfiusLoginTestData.SCA_REQUIRED_DUE_TO_INACTIVITY);
        when(belfiusApiClientMock.loginPw(
                        DEVICE_TOKEN_HASHED, DEVICE_TOKEN_HASHED_HASHED, SIGNATURE_PW))
                .thenReturn(loginResponse);

        // when
        final Throwable thrown = catchThrowable(belfiusAuthenticator::autoAuthenticate);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(SessionException.class)
                .hasNoCause()
                .hasMessage("SCA required do to an extended period of inactivity.");
    }

    private static BelfiusSignatureCreator createBelfiusSignatureCreatorMock() {
        final BelfiusSignatureCreator belfiusSignatureCreatorMock =
                mock(BelfiusSignatureCreator.class);

        when(belfiusSignatureCreatorMock.hash(DEVICE_TOKEN)).thenReturn(DEVICE_TOKEN_HASHED);
        when(belfiusSignatureCreatorMock.hash(DEVICE_TOKEN_HASHED))
                .thenReturn(DEVICE_TOKEN_HASHED_HASHED);
        when(belfiusSignatureCreatorMock.createSignatureSoft(CHALLENGE, DEVICE_TOKEN, PAN_NUMBER))
                .thenReturn(SIGNATURE_SOFT);
        when(belfiusSignatureCreatorMock.createSignaturePw(
                        CHALLENGE, DEVICE_TOKEN, PAN_NUMBER, CONTRACT_NUMBER, PASSWORD))
                .thenReturn(SIGNATURE_PW);

        return belfiusSignatureCreatorMock;
    }

    private static BelfiusApiClient createApiClientMock() throws LoginException {
        final BelfiusApiClient belfiusApiClientMock = mock(BelfiusApiClient.class);

        when(belfiusApiClientMock.isDeviceRegistered(PAN_NUMBER, DEVICE_TOKEN_HASHED))
                .thenReturn(true);

        final PrepareLoginResponse prepareLoginResponseMock = mock(PrepareLoginResponse.class);
        when(prepareLoginResponseMock.getContractNumber()).thenReturn(CONTRACT_NUMBER);
        when(prepareLoginResponseMock.getChallenge()).thenReturn(CHALLENGE);

        when(belfiusApiClientMock.prepareLogin(PAN_NUMBER)).thenReturn(prepareLoginResponseMock);

        final SendCardNumberResponse sendCardNumberResponseMock =
                mock(SendCardNumberResponse.class);
        when(sendCardNumberResponseMock.getChallenge()).thenReturn(CHALLENGE);

        when(belfiusApiClientMock.sendCardNumber(PAN_NUMBER))
                .thenReturn(sendCardNumberResponseMock);

        return belfiusApiClientMock;
    }

    private static Credentials createCredentialsMock() {
        final Credentials credentialsMock = mock(Credentials.class);

        when(credentialsMock.getField(Field.Key.USERNAME)).thenReturn(PAN_NUMBER);
        when(credentialsMock.getField(Field.Key.PASSWORD)).thenReturn(PASSWORD);

        return credentialsMock;
    }

    private static PersistentStorage createPersistentStorageMock() {
        final PersistentStorage persistentStorageMock = mock(PersistentStorage.class);

        when(persistentStorageMock.get(BelfiusConstants.Storage.DEVICE_TOKEN))
                .thenReturn(DEVICE_TOKEN);

        return persistentStorageMock;
    }
}
