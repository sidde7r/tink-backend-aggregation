package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.SessionOpenedResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusStringUtils;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;

public class IsDeviceRegisteredStepTest extends BaseStep {

    @Test
    public void shouldGotoRegisterDeviceStepWhenNoDeviceRegistered() {
        // given
        AgentPlatformBelfiusApiClient apiClient = Mockito.mock(AgentPlatformBelfiusApiClient.class);
        IsDeviceRegisteredStep step =
                new IsDeviceRegisteredStep(apiClient, createBelfiusDataAccessorFactory());

        AgentProceedNextStepAuthenticationRequest request =
                createAgentProceedNextStepAuthenticationRequest(
                        new BelfiusProcessState(), new BelfiusAuthenticationData());

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then
        assertThat(result).isInstanceOf(AgentProceedNextStepAuthenticationResult.class);
        AgentProceedNextStepAuthenticationResult nextStepResult =
                (AgentProceedNextStepAuthenticationResult) result;
        assertThat(nextStepResult.getAuthenticationProcessStepIdentifier().get())
                .isEqualTo(
                        AgentAuthenticationProcessStepIdentifier.of(
                                RegisterDeviceStartStep.class.getSimpleName()));
    }

    @Test
    public void shouldGotoSoftLoginInitStepWhenDeviceRegistered() {
        // given
        AgentPlatformBelfiusApiClient apiClient = Mockito.mock(AgentPlatformBelfiusApiClient.class);
        IsDeviceRegisteredStep step =
                new IsDeviceRegisteredStep(apiClient, createBelfiusDataAccessorFactory());

        AgentProceedNextStepAuthenticationRequest request =
                createAgentProceedNextStepAuthenticationRequest(
                        new BelfiusProcessState().sessionId(SESSION_ID).machineId(MACHINE_ID),
                        new BelfiusAuthenticationData()
                                .panNumber(PAN_NUMBER)
                                .deviceToken(DEVICE_TOKEN));

        // when

        when(apiClient.openSession("XXX"))
                .thenReturn(new SessionOpenedResponse(SESSION_ID, MACHINE_ID, 1));

        when(apiClient.isDeviceRegistered(
                        SESSION_ID,
                        MACHINE_ID,
                        "2",
                        BelfiusStringUtils.formatPanNumber(PAN_NUMBER),
                        DEVICE_TOKEN))
                .thenReturn(true);

        AgentAuthenticationResult result = step.execute(request);

        // then
        assertThat(result).isInstanceOf(AgentProceedNextStepAuthenticationResult.class);
        AgentProceedNextStepAuthenticationResult nextStepResult =
                (AgentProceedNextStepAuthenticationResult) result;
        assertThat(nextStepResult.getAuthenticationProcessStepIdentifier().get())
                .isEqualTo(
                        AgentAuthenticationProcessStepIdentifier.of(
                                SoftLoginInitStep.class.getSimpleName()));

        verify(apiClient).startFlow(SESSION_ID, MACHINE_ID, "1");
    }
}
