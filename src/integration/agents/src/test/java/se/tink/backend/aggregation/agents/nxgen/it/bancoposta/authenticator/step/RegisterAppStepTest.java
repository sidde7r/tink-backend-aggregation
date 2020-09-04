package se.tink.backend.aggregation.agents.nxgen.it.bancoposta.authenticator.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.it.bancoposta.authenticator.AuthenticationTestData;
import se.tink.backend.aggregation.agents.nxgen.it.bancoposta.authenticator.AuthenticationTestHelper;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaStorage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.RegisterAppStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;

public class RegisterAppStepTest {
    private RegisterAppStep objUnderTest;
    private BancoPostaApiClient apiClient;
    private BancoPostaStorage storage;
    private AuthenticationRequest request;

    @Before
    public void init() {
        this.apiClient = Mockito.mock(BancoPostaApiClient.class);
        this.storage = AuthenticationTestHelper.prepareStorageForTests();
        this.objUnderTest = new RegisterAppStep(apiClient, storage);
        this.request = new AuthenticationRequest(Mockito.mock(Credentials.class));
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
        given(storage.isUserPinSetRequired()).willReturn(true);
        // when
        objUnderTest.execute(request);
        // then
        verify(storage).saveToPersistentStorage(Storage.USER_PIN_SET_REQUIRED, true);
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
