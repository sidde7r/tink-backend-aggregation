package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.security.PublicKey;
import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Error;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.ScaMethod;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Values;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.InitScaEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.LoginInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.StatusEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.ApprovalResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.InitScaResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.ErrorEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.ErrorMessageEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.MetaDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.rpc.ResultEntity;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.Catalog;

@RunWith(MockitoJUnitRunner.class)
public class CommerzbankPhotoTanAuthenticatorTest {

    private static final String USERNAME = "sampel username";
    private static final String PASSWORD = "sample password";
    private static final String PROCESS_CONTEXT_ID = "process context id";
    private static final String APPROVAL_STATUS_OK = "OK";
    private static final String APPROVAL_STATUS_FAIL = "FAIL";
    private static final String APP_ID = "init app registration id";

    private CommerzbankPhotoTanAuthenticator authenticator;

    private PersistentStorage persistentStorage;
    private CommerzbankApiClient apiClient;
    private SupplementalInformationController supplementalInformationController;
    private Catalog catalog;

    private Credentials credentials;

    @Before
    public void setup() {
        persistentStorage = mock(PersistentStorage.class);
        apiClient = mock(CommerzbankApiClient.class);
        supplementalInformationController = mock(SupplementalInformationController.class);
        catalog = Catalog.getCatalog("EN_US");

        authenticator =
                new CommerzbankPhotoTanAuthenticator(
                        persistentStorage, apiClient, supplementalInformationController, catalog);

        credentials = mock(Credentials.class);
        given(credentials.getField(Key.USERNAME)).willReturn(USERNAME);
        given(credentials.getField(Key.PASSWORD)).willReturn(PASSWORD);

        given(apiClient.manualLogin(USERNAME, PASSWORD))
                .willReturn(loginResponseWithStatus(Values.TAN_REQUESTED));

        given(apiClient.initScaFlow()).willReturn(getInitScaResponse(ScaMethod.PUSH_PHOTO_TAN));

        given(apiClient.approveSca(PROCESS_CONTEXT_ID))
                .willReturn(getApprovalResponse(APPROVAL_STATUS_OK));
        given(apiClient.initAppRegistration()).willReturn(APP_ID);
    }

    @Test
    public void authenticatorShouldBePasswordTyped() {
        // given

        // when
        CredentialsTypes result = authenticator.getType();

        // then
        assertThat(result).isEqualTo(CredentialsTypes.PASSWORD);
    }

    @Test
    public void authenticateShouldThrowExceptionWhenCredentialsHasNullPassword() {
        // given
        given(credentials.getField(Key.PASSWORD)).willReturn(null);

        // when
        Throwable t = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void authenticateShouldThrowExceptionWhenCredentialsHasEmptyPassword() {
        // given
        given(credentials.getField(Key.PASSWORD)).willReturn("");

        // when
        Throwable t = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void authenticateShouldThrowExceptionWhenCredentialsHasNullUsername() {
        // given
        given(credentials.getField(Key.USERNAME)).willReturn(null);

        // when
        Throwable t = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void authenticateShouldThrowExceptionWhenCredentialsHasEmptyUsername() {
        // given
        given(credentials.getField(Key.USERNAME)).willReturn("");

        // when
        Throwable t = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void authenticateShouldThrowLoginExceptionWhenLoginResponseContainsPinError() {
        // given
        LoginResponse loginResponse = loginResponseWithError(Error.PIN_ERROR);
        given(apiClient.manualLogin(USERNAME, PASSWORD)).willReturn(loginResponse);

        // when
        Throwable t = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void
            authenticateShouldThrowLoginExceptionWhenLoginResponseContainsValidationException() {
        // given
        LoginResponse loginResponse = loginResponseWithError(Error.VALIDATION_EXCEPTION);
        given(apiClient.manualLogin(USERNAME, PASSWORD)).willReturn(loginResponse);

        // when
        Throwable t = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void
            authenticateShouldThrowSessionExceptionWhenLoginResponseContainsSessionActiveError() {
        // given
        LoginResponse loginResponse = loginResponseWithError(Error.ACCOUNT_SESSION_ACTIVE_ERROR);
        given(apiClient.manualLogin(USERNAME, PASSWORD)).willReturn(loginResponse);

        // when
        Throwable t = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(t)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_ALREADY_ACTIVE");
    }

    @Test
    public void authenticateShouldThrowDefaultLoginErrorWhenLoginResponseContainsUnknownError() {
        // given
        LoginResponse loginResponse = loginResponseWithError("UNKNOWN ERROR");
        given(apiClient.manualLogin(USERNAME, PASSWORD)).willReturn(loginResponse);

        // when
        Throwable t = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(t).isInstanceOf(LoginException.class).hasMessage("Reason: UNKNOWN ERROR");
    }

    @Test
    public void authenticateShouldThrowLoginExceptionWhenLoginResponseContainsNullError() {
        // given
        LoginResponse loginResponse = loginResponseWithError(null);
        given(apiClient.manualLogin(USERNAME, PASSWORD)).willReturn(loginResponse);

        // when
        Throwable t = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.DEFAULT_MESSAGE");
    }

    @Test
    public void authenticateShouldThrowLoginExceptionWhenLoginStatusIsTanNotActive() {
        // given
        given(apiClient.manualLogin(USERNAME, PASSWORD))
                .willReturn(loginResponseWithStatus(Values.TAN_NOTACTIVE));

        // when
        Throwable t = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.NO_AVAILABLE_SCA_METHODS");
        // and
        verify(credentials)
                .setSensitivePayload(
                        CommerzbankConstants.LOGIN_INFO_ENTITY,
                        "{\"challenge\":\"CHALLENGE\",\"loginStatus\":\"TAN_NOTACTIVE\"}");
    }

    @Test
    public void authenticateShouldThrowGeneralLoginExceptionWhenLoginStatusIsTotallyDifferent() {
        // given
        given(apiClient.manualLogin(USERNAME, PASSWORD))
                .willReturn(loginResponseWithStatus("TOTALLY_DIFFERENT"));

        // when
        Throwable t = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.DEFAULT_MESSAGE");
        // and
        verify(credentials)
                .setSensitivePayload(
                        CommerzbankConstants.LOGIN_INFO_ENTITY,
                        "{\"challenge\":\"CHALLENGE\",\"loginStatus\":\"TOTALLY_DIFFERENT\"}");
    }

    @Test
    public void authenticateShouldThrowLoginNotSupportedExceptionWhenPushPhotoTanIsNotAvailable() {
        // given
        given(apiClient.initScaFlow())
                .willReturn(getInitScaResponse("not" + ScaMethod.PUSH_PHOTO_TAN));

        // when
        Throwable t = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.NOT_SUPPORTED");
    }

    @Test
    public void
            authenticationShouldThrowIncorrectChallengeResponseExceptionWhenFailedToApproveSca3Times() {
        // given
        int numOfAttempts = 3;
        authenticator =
                new CommerzbankPhotoTanAuthenticator(
                        persistentStorage,
                        apiClient,
                        supplementalInformationController,
                        10,
                        numOfAttempts,
                        catalog);
        // and
        given(apiClient.approveSca(PROCESS_CONTEXT_ID))
                .willReturn(getApprovalResponse(APPROVAL_STATUS_FAIL));

        // when
        Throwable t = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CHALLENGE_RESPONSE");
    }

    @Test
    public void authenticateShouldRequestSupplementalInformationAndPinDeviceWhenSucceed()
            throws AuthenticationException, AuthorizationException {
        // given

        // when
        authenticator.authenticate(credentials);

        // then
        verify(apiClient).prepareScaApproval(PROCESS_CONTEXT_ID);
        verify(supplementalInformationController).askSupplementalInformationAsync(any());
        verify(apiClient).finaliseScaApproval(PROCESS_CONTEXT_ID);

        verify(persistentStorage).put(Storage.APP_ID, APP_ID);

        verify(apiClient).completeAppRegistration(APP_ID);

        verify(persistentStorage).put(argThat(t -> t.equals(Storage.KEY_PAIR)), anyString());
        verify(apiClient).send2FactorToken(argThat(t -> t.equals(APP_ID)), any(PublicKey.class));
    }

    private LoginResponse loginResponseWithError(final String error) {
        ErrorEntity errorEntity = mock(ErrorEntity.class);

        if (error != null) {
            ErrorMessageEntity errorMessageEntity = mock(ErrorMessageEntity.class);
            given(errorMessageEntity.getMessageId()).willReturn(error);
            given(errorEntity.getErrorMessage()).willReturn(Optional.of(errorMessageEntity));
        } else {
            given(errorEntity.getErrorMessage()).willReturn(Optional.empty());
        }

        LoginResponse loginResponse = mock(LoginResponse.class);
        given(loginResponse.getError()).willReturn(errorEntity);

        return loginResponse;
    }

    private LoginResponse loginResponseWithStatus(final String loginStatus) {
        return new LoginResponse(
                new ResultEntity<>(new LoginInfoEntity(Values.CHALLENGE, loginStatus), null));
    }

    private InitScaResponse getInitScaResponse(final String scaMethod) {
        return new InitScaResponse(
                new ResultEntity<>(new InitScaEntity(Collections.singletonList(scaMethod)), null),
                null,
                new MetaDataEntity(PROCESS_CONTEXT_ID));
    }

    private ApprovalResponse getApprovalResponse(String approvalStatus) {
        return new ApprovalResponse(
                new ResultEntity<>(new StatusEntity(null, approvalStatus, null), null));
    }
}
