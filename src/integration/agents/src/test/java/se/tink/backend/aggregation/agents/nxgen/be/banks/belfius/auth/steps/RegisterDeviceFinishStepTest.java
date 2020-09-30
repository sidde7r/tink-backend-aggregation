package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;

public class RegisterDeviceFinishStepTest extends BaseStepTest {

    @Test
    public void shouldSaveDeviceTokenAndCloseSession() {
        // given
        AgentPlatformBelfiusApiClient apiClient = Mockito.mock(AgentPlatformBelfiusApiClient.class);
        RegisterDeviceFinishStep step =
                new RegisterDeviceFinishStep(
                        apiClient, createBelfiusPersistedDataAccessorFactory());

        AgentProceedNextStepAuthenticationRequest request =
                createAgentProceedNextStepAuthenticationRequest(
                        BelfiusProcessState.builder()
                                .sessionId(SESSION_ID)
                                .machineId(MACHINE_ID)
                                .deviceToken(DEVICE_TOKEN)
                                .build(),
                        new BelfiusAuthenticationData());

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then
        assertThat(result).isInstanceOf(AgentProceedNextStepAuthenticationResult.class);
        AgentProceedNextStepAuthenticationResult nextStepResult =
                (AgentProceedNextStepAuthenticationResult) result;
        assertThat(nextStepResult.getAuthenticationProcessStepIdentifier().get())
                .isEqualTo(
                        AgentAuthenticationProcessStepIdentifier.of(
                                SoftLoginInitStep.class.getSimpleName()));

        verify(apiClient).closeSession(SESSION_ID, MACHINE_ID, "1");

        BelfiusAuthenticationData persistence =
                createBelfiusPersistedDataAccessorFactory()
                        .createBelfiusPersistedDataAccessor(
                                nextStepResult.getAuthenticationPersistedData())
                        .getBelfiusAuthenticationData();
        assertThat(nextStepResult.getAuthenticationProcessState()).isPresent();

        assertThat(persistence.getDeviceToken()).isEqualTo(DEVICE_TOKEN);
    }
}
