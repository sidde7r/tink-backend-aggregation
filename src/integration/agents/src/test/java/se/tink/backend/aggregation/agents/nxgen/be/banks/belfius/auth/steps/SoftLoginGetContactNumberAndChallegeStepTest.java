package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusStringUtils;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;

public class SoftLoginGetContactNumberAndChallegeStepTest extends BaseStep {

    @Test
    public void shouldGetContractNumberAndChallenge() {
        // given
        AgentPlatformBelfiusApiClient apiClient = Mockito.mock(AgentPlatformBelfiusApiClient.class);
        SoftLoginGetContactNumberAndChallegeStep step =
                new SoftLoginGetContactNumberAndChallegeStep(
                        apiClient, createBelfiusDataAccessorFactory());

        AgentProceedNextStepAuthenticationRequest request =
                createAgentProceedNextStepAuthenticationRequest(
                        new BelfiusProcessState().sessionId(SESSION_ID).machineId(MACHINE_ID),
                        new BelfiusAuthenticationData().panNumber(PAN_NUMBER));

        when(apiClient.prepareLogin(
                        SESSION_ID,
                        MACHINE_ID,
                        "1",
                        BelfiusStringUtils.formatPanNumber(PAN_NUMBER)))
                .thenReturn(
                        new PrepareLoginResponse() {
                            public String getChallenge() {
                                return CHALLENGE;
                            }

                            public String getContractNumber() {
                                return CONTRACT_NUMBER;
                            }
                        });

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then
        assertThat(result).isInstanceOf(AgentProceedNextStepAuthenticationResult.class);
        AgentProceedNextStepAuthenticationResult nextStepResult =
                (AgentProceedNextStepAuthenticationResult) result;
        assertThat(nextStepResult.getAuthenticationProcessStepIdentifier().get())
                .isEqualTo(
                        AgentAuthenticationProcessStepIdentifier.of(
                                SoftLoginStep.class.getSimpleName()));

        BelfiusProcessState processState =
                createBelfiusDataAccessorFactory()
                        .createBelfiusProcessStateAccessor(
                                nextStepResult.getAuthenticationProcessState().get())
                        .getBelfiusProcessState();
        assertThat(processState.getChallenge()).isEqualTo(CHALLENGE);
        assertThat(processState.getContractNumber()).isEqualTo(CONTRACT_NUMBER);
    }
}
