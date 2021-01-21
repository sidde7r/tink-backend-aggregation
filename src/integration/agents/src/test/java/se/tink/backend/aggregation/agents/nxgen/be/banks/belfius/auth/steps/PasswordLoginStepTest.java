package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.responsevalidator.AgentPlatformResponseValidator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.PasswordLoginStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccountBlockedError;

public class PasswordLoginStepTest extends BaseStep {

    @Test
    public void shouldReturnErrorWhenAuthenticationFailed() {
        // given
        AgentPlatformBelfiusApiClient apiClient = Mockito.mock(AgentPlatformBelfiusApiClient.class);
        BelfiusSessionStorage sessionStorage = Mockito.mock(BelfiusSessionStorage.class);
        AgentPlatformResponseValidator validator =
                Mockito.mock(AgentPlatformResponseValidator.class);
        PasswordLoginStep step =
                new PasswordLoginStep(
                        apiClient, sessionStorage, createBelfiusDataAccessorFactory(), validator);

        AgentProceedNextStepAuthenticationRequest request =
                createAgentProceedNextStepAuthenticationRequest(
                        new BelfiusProcessState()
                                .sessionId(SESSION_ID)
                                .machineId(MACHINE_ID)
                                .deviceTokenHashed(DEVICE_TOKEN_HASHED)
                                .deviceTokenHashedIosComparison(DEVICE_TOKEN_HASHED_IOS_COMPARISON)
                                .encryptedPassword(ENCRYPTED_PASSWORD),
                        new BelfiusAuthenticationData(),
                        AgentExtendedClientInfo.builder().build());

        when(apiClient.loginPw(
                        SESSION_ID,
                        MACHINE_ID,
                        "1",
                        DEVICE_TOKEN_HASHED,
                        DEVICE_TOKEN_HASHED_IOS_COMPARISON,
                        ENCRYPTED_PASSWORD))
                .thenReturn(Mockito.mock(LoginResponse.class));

        when(validator.validate(any(LoginResponse.class)))
                .thenReturn(Optional.of(new AccountBlockedError()));

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then
        assertThat(result).isInstanceOf(AgentFailedAuthenticationResult.class);
        AgentFailedAuthenticationResult failedResult = (AgentFailedAuthenticationResult) result;
        assertThat(failedResult.getError()).isInstanceOf(AccountBlockedError.class);
    }

    @Test
    public void shouldSucceedWhenSuccessfulAuth() {
        // given
        AgentPlatformBelfiusApiClient apiClient = Mockito.mock(AgentPlatformBelfiusApiClient.class);
        BelfiusSessionStorage sessionStorage = Mockito.mock(BelfiusSessionStorage.class);
        AgentPlatformResponseValidator validator =
                Mockito.mock(AgentPlatformResponseValidator.class);
        PasswordLoginStep step =
                new PasswordLoginStep(
                        apiClient, sessionStorage, createBelfiusDataAccessorFactory(), validator);

        AgentProceedNextStepAuthenticationRequest request =
                createAgentProceedNextStepAuthenticationRequest(
                        new BelfiusProcessState()
                                .sessionId(SESSION_ID)
                                .machineId(MACHINE_ID)
                                .deviceTokenHashed(DEVICE_TOKEN_HASHED)
                                .deviceTokenHashedIosComparison(DEVICE_TOKEN_HASHED_IOS_COMPARISON)
                                .encryptedPassword(ENCRYPTED_PASSWORD),
                        new BelfiusAuthenticationData(),
                        AgentExtendedClientInfo.builder().build());

        when(validator.validate(any(LoginResponse.class))).thenReturn(Optional.empty());

        // when

        when(apiClient.loginPw(
                        SESSION_ID,
                        MACHINE_ID,
                        "1",
                        DEVICE_TOKEN_HASHED,
                        DEVICE_TOKEN_HASHED_IOS_COMPARISON,
                        ENCRYPTED_PASSWORD))
                .thenReturn(Mockito.mock(LoginResponse.class));

        AgentAuthenticationResult result = step.execute(request);

        // then
        assertThat(result).isInstanceOf(AgentSucceededAuthenticationResult.class);
    }
}
