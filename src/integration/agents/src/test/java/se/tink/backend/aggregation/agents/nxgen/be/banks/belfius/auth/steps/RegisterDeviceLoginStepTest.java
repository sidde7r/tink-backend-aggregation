package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.AuthenticateWithCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.SessionOpenedResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.AgentFieldValue;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.CardReaderLoginInputAgentField;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.IncorrectCardReaderResponseCodeError;

public class RegisterDeviceLoginStepTest extends BaseStep {

    @Test
    public void shouldAuthenticateWithCode() {
        // given
        AgentPlatformBelfiusApiClient apiClient = mockApiClient(true);
        RegisterDeviceLoginStep step =
                new RegisterDeviceLoginStep(apiClient, createBelfiusDataAccessorFactory());

        AgentUserInteractionAuthenticationProcessRequest request =
                createAgentUserInteractionAuthenticationProcessRequest(
                        new BelfiusProcessState().sessionId(SESSION_ID).machineId(MACHINE_ID),
                        new BelfiusAuthenticationData(),
                        new AgentFieldValue(CardReaderLoginInputAgentField.id(), CODE));

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then

        assertThat(result).isInstanceOf(AgentProceedNextStepAuthenticationResult.class);
        AgentProceedNextStepAuthenticationResult nextStepResult =
                (AgentProceedNextStepAuthenticationResult) result;
        assertThat(nextStepResult.getAuthenticationProcessStepIdentifier().get())
                .isEqualTo(
                        AgentAuthenticationProcessStepIdentifier.of(
                                RegisterDeviceGetSignCodeStep.class.getSimpleName()));

        verify(apiClient).keepAlive(SESSION_ID, MACHINE_ID, "1");
    }

    @Test
    public void shouldReturnFailedResult() {
        // given
        AgentPlatformBelfiusApiClient apiClient = mockApiClient(false);
        RegisterDeviceLoginStep step =
                new RegisterDeviceLoginStep(apiClient, createBelfiusDataAccessorFactory());

        AgentUserInteractionAuthenticationProcessRequest request =
                createAgentUserInteractionAuthenticationProcessRequest(
                        new BelfiusProcessState().sessionId(SESSION_ID).machineId(MACHINE_ID),
                        new BelfiusAuthenticationData(),
                        new AgentFieldValue(CardReaderLoginInputAgentField.id(), CODE));

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then

        assertThat(result).isInstanceOf(AgentFailedAuthenticationResult.class);
        AgentFailedAuthenticationResult failedResult = (AgentFailedAuthenticationResult) result;
        assertThat(failedResult.getError().getClass())
                .isEqualTo(IncorrectCardReaderResponseCodeError.class);
    }

    private AgentPlatformBelfiusApiClient mockApiClient(boolean authenticateWithCodeResponseValid) {
        AgentPlatformBelfiusApiClient apiClient = mock(AgentPlatformBelfiusApiClient.class);
        when(apiClient.openSession("XXX"))
                .thenReturn(new SessionOpenedResponse(SESSION_ID, MACHINE_ID, 1));

        when(apiClient.prepareLogin(SESSION_ID, MACHINE_ID, "2", PAN_NUMBER))
                .thenReturn(mock(PrepareLoginResponse.class, a -> CONTRACT_NUMBER));

        AuthenticateWithCodeResponse authenticateWithCodeResponse =
                mockAuthenticateWithCodeResponse(authenticateWithCodeResponseValid);
        when(apiClient.authenticateWithCode(SESSION_ID, MACHINE_ID, "2", CODE))
                .thenReturn(authenticateWithCodeResponse);

        return apiClient;
    }

    private AuthenticateWithCodeResponse mockAuthenticateWithCodeResponse(boolean valid) {
        AuthenticateWithCodeResponse response = mock(AuthenticateWithCodeResponse.class);
        when(response.isChallengeResponseOk()).thenReturn(valid);
        return response;
    }
}
