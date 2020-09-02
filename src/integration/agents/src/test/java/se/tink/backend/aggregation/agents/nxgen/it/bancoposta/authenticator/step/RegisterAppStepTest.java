package se.tink.backend.aggregation.agents.nxgen.it.bancoposta.authenticator.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.UserContext;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.RegisterAppStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RegisterAppStepTest {
    private RegisterAppStep objUnderTest;
    private BancoPostaApiClient apiClient;
    private UserContext userContext;
    private AuthenticationRequest request;

    @Before
    public void init() {
        this.apiClient = Mockito.mock(BancoPostaApiClient.class);
        this.userContext = new UserContext(new PersistentStorage());
        this.objUnderTest = new RegisterAppStep(apiClient, userContext);
        this.request = new AuthenticationRequest(Mockito.mock(Credentials.class));

        this.userContext.saveToPersistentStorage(Storage.APP_ID, AuthenticationTestData.APP_ID);
        this.userContext.saveToPersistentStorage(
                Storage.PUB_SERVER_KEY, AuthenticationTestData.PUB_KEY);
        this.userContext.saveToPersistentStorage(
                Storage.OTP_SECRET_KEY, AuthenticationTestData.OTP_SECRET_KEY);
    }

    @Test
    public void executeShouldThrowErrorIfMaxLimitOfDevicesReached() {
        // given
        given(apiClient.registerApp(any()))
                .willReturn(AuthenticationTestData.registerAppResponseWithMaxDeviceReachedError());
        // when
        Throwable throwable = Assertions.catchThrowable(() -> objUnderTest.execute(request));
        // then
        assertThat(throwable)
                .isInstanceOf(LoginException.class)
                .hasMessageContaining(LoginError.REGISTER_DEVICE_ERROR.name());
    }

    @Test
    public void executeShouldSetFlagUserPinSetRequiredToTrueIfPinHasToBeSet() {
        // given
        given(apiClient.registerApp(any()))
                .willReturn(AuthenticationTestData.registerAppResponseWithPinError());
        // when
        objUnderTest.execute(request);
        // then
        assertThat(userContext.isUserPinSetRequired()).isEqualTo(true);
    }

    @Test
    public void executeShouldThrowLoginExcpetionIfCodeErrorIsUnknown() {
        // given
        given(apiClient.registerApp(any()))
                .willReturn(AuthenticationTestData.registerAppResponseWithDefaultError());
        // when
        Throwable throwable = Assertions.catchThrowable(() -> objUnderTest.execute(request));
        // then
        assertThat(throwable)
                .isInstanceOf(LoginException.class)
                .hasMessageContaining("Unknown register app error number UNKNOWN_CODE");
    }
}
