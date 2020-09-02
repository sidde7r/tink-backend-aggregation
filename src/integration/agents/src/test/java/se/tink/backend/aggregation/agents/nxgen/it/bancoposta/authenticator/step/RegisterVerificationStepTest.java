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
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.UserContext;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.RegisterVerificationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RegisterVerificationStepTest {
    private RegisterVerificationStep objUnderTest;
    private BancoPostaApiClient apiClient;
    private UserContext userContext;
    private AuthenticationRequest request;

    @Before
    public void init() {
        this.apiClient = Mockito.mock(BancoPostaApiClient.class);
        this.userContext = new UserContext(Mockito.mock(PersistentStorage.class));
        this.objUnderTest = new RegisterVerificationStep(apiClient, userContext);
        this.request = new AuthenticationRequest(Mockito.mock(Credentials.class));
    }

    @Test
    public void executeShouldReturnSyncWalletStepIfSyncWalletIsRequired() {
        // given
        given(apiClient.verifyOnboarding(any()))
                .willReturn(
                        AuthenticationTestData.verificationOnboardingResponse(true, false, "\"\""));
        // when
        AuthenticationStepResponse response = objUnderTest.execute(request);
        // then
        assertThat(response.getNextStepId().get()).isEqualTo("onboardingStep");
    }

    @Test
    public void executeShouldReturnOnboardingStepIfOnboardingIsRequired() {
        // given
        given(apiClient.verifyOnboarding(any()))
                .willReturn(
                        AuthenticationTestData.verificationOnboardingResponse(false, true, "\"\""));
        // when
        AuthenticationStepResponse response = objUnderTest.execute(request);
        // then
        assertThat(response.getNextStepId().get()).isEqualTo("syncWalletStep");
    }

    @Test
    public void executeShouldReturnRegisterAppStepIfRegisterAppAvailable() {
        // given
        given(apiClient.verifyOnboarding(any()))
                .willReturn(
                        AuthenticationTestData.verificationOnboardingResponse(
                                false, false, "\"registerToken\""));
        // when
        AuthenticationStepResponse response = objUnderTest.execute(request);
        // then
        assertThat(response.getNextStepId().get()).isEqualTo("registerAppStep");
    }

    @Test
    public void
            executeShouldThrowLoginExceptionIfOnboardingAndSyncWalletNotRequiredAndRegisterTokenNotAvailable() {
        // given
        given(apiClient.verifyOnboarding(any()))
                .willReturn(
                        AuthenticationTestData.verificationOnboardingResponse(
                                false, false, "\"\""));
        // when
        Throwable throwable = Assertions.catchThrowable(() -> objUnderTest.execute(request));
        // then
        assertThat(throwable).isInstanceOf(LoginException.class);
    }

    @Test
    public void executeShouldAddAccountNumberToUserContextIfAvailable() {
        // given
        given(apiClient.verifyOnboarding(any()))
                .willReturn(
                        AuthenticationTestData.verificationOnboardingResponse(
                                false, false, "\"registerToken\""));
        // when
        objUnderTest.execute(request);
        // then
        assertThat(this.userContext.getAccountNumber()).isNotBlank();
        assertThat(this.userContext.getAccountNumber()).isNotNull();
    }
}
