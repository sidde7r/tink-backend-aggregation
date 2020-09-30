package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.responsevalidator.LoginResponseStatus;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;

public class PasswordLoginStepTest extends BaseStepTest {

    @Test
    public void shouldReturnErrorWhenAuthenticationFailed() {
        // given
        AgentPlatformBelfiusApiClient apiClient = Mockito.mock(AgentPlatformBelfiusApiClient.class);
        BelfiusSessionStorage sessionStorage = Mockito.mock(BelfiusSessionStorage.class);
        PasswordLoginStep step = new PasswordLoginStep(apiClient, sessionStorage);

        AgentProceedNextStepAuthenticationRequest request =
                createAgentProceedNextStepAuthenticationRequest(
                        BelfiusProcessState.builder()
                                .sessionId(SESSION_ID)
                                .machineId(MACHINE_ID)
                                .deviceTokenHashed(DEVICE_TOKEN_HASHED)
                                .deviceTokenHashedIosComparison(DEVICE_TOKEN_HASHED_IOS_COMPARISON)
                                .encryptedPassword(ENCRYPTED_PASSWORD)
                                .build(),
                        new BelfiusAuthenticationData());

        when(apiClient.loginPw(
                        SESSION_ID,
                        MACHINE_ID,
                        "1",
                        DEVICE_TOKEN_HASHED,
                        DEVICE_TOKEN_HASHED_IOS_COMPARISON,
                        ENCRYPTED_PASSWORD))
                .thenReturn(
                        Mockito.mock(
                                LoginResponse.class, a -> LoginResponseStatus.ACCOUNT_BLOCKED));

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then
        assertThat(result).isInstanceOf(AgentFailedAuthenticationResult.class);
        AgentFailedAuthenticationResult failedResult = (AgentFailedAuthenticationResult) result;
        assertThat(failedResult.getError())
                .isEqualTo(LoginResponseStatus.ACCOUNT_BLOCKED.getError());
    }

    @Test
    public void shouldSucceedWhenSuccessfulAuth() {
        // given
        AgentPlatformBelfiusApiClient apiClient = Mockito.mock(AgentPlatformBelfiusApiClient.class);
        BelfiusSessionStorage sessionStorage = Mockito.mock(BelfiusSessionStorage.class);
        PasswordLoginStep step = new PasswordLoginStep(apiClient, sessionStorage);

        AgentProceedNextStepAuthenticationRequest request =
                createAgentProceedNextStepAuthenticationRequest(
                        BelfiusProcessState.builder()
                                .sessionId(SESSION_ID)
                                .machineId(MACHINE_ID)
                                .deviceTokenHashed(DEVICE_TOKEN_HASHED)
                                .deviceTokenHashedIosComparison(DEVICE_TOKEN_HASHED_IOS_COMPARISON)
                                .encryptedPassword(ENCRYPTED_PASSWORD)
                                .build(),
                        new BelfiusAuthenticationData());

        // when

        when(apiClient.loginPw(
                        SESSION_ID,
                        MACHINE_ID,
                        "1",
                        DEVICE_TOKEN_HASHED,
                        DEVICE_TOKEN_HASHED_IOS_COMPARISON,
                        ENCRYPTED_PASSWORD))
                .thenReturn(Mockito.mock(LoginResponse.class, a -> LoginResponseStatus.NO_ERRORS));

        AgentAuthenticationResult result = step.execute(request);

        // then
        assertThat(result).isInstanceOf(AgentSucceededAuthenticationResult.class);
    }
}
