package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.RegisterDeviceSignResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.RegisterDeviceFinishStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.RegisterDeviceSignStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.AgentFieldValue;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.CardReaderSignInputAgentField;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;

public class RegisterDeviceSignStepTest extends BaseStep {

    @Test
    public void shouldRegisterDevice() {
        // given
        AgentPlatformBelfiusApiClient apiClient = Mockito.mock(AgentPlatformBelfiusApiClient.class);
        RegisterDeviceSignStep step =
                new RegisterDeviceSignStep(apiClient, createBelfiusDataAccessorFactory());

        AgentUserInteractionAuthenticationProcessRequest request =
                createAgentUserInteractionAuthenticationProcessRequest(
                        new BelfiusProcessState().sessionId(SESSION_ID).machineId(MACHINE_ID),
                        new BelfiusAuthenticationData(),
                        AgentExtendedClientInfo.builder().build(),
                        new AgentFieldValue(CardReaderSignInputAgentField.id(), CODE));

        RegisterDeviceSignResponse registerDeviceSignResponse =
                mockValidRegisterDeviceSignResponse();
        when(apiClient.registerDevice(SESSION_ID, MACHINE_ID, "1", CODE))
                .thenReturn(registerDeviceSignResponse);

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then

        assertThat(result).isInstanceOf(AgentProceedNextStepAuthenticationResult.class);
        AgentProceedNextStepAuthenticationResult nextStepResult =
                (AgentProceedNextStepAuthenticationResult) result;
        assertThat(nextStepResult.getAuthenticationProcessStepIdentifier().get())
                .isEqualTo(
                        AgentAuthenticationProcessStepIdentifier.of(
                                RegisterDeviceFinishStep.class.getSimpleName()));
    }

    @Test
    public void shouldReturnFailedResult() {
        // given
        AgentPlatformBelfiusApiClient apiClient = Mockito.mock(AgentPlatformBelfiusApiClient.class);
        RegisterDeviceSignStep step =
                new RegisterDeviceSignStep(apiClient, createBelfiusDataAccessorFactory());

        AgentUserInteractionAuthenticationProcessRequest request =
                createAgentUserInteractionAuthenticationProcessRequest(
                        new BelfiusProcessState().sessionId(SESSION_ID).machineId(MACHINE_ID),
                        new BelfiusAuthenticationData(),
                        AgentExtendedClientInfo.builder().build(),
                        new AgentFieldValue(CardReaderSignInputAgentField.id(), CODE));

        RegisterDeviceSignResponse registerDeviceSignResponse =
                mockInvalidRegisterDeviceSignResponse();
        when(apiClient.registerDevice(SESSION_ID, MACHINE_ID, "1", CODE))
                .thenReturn(registerDeviceSignResponse);

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then

        assertThat(result).isInstanceOf(AgentFailedAuthenticationResult.class);
        AgentFailedAuthenticationResult failedResult = (AgentFailedAuthenticationResult) result;
        assertThat(failedResult.getError())
                .isEqualTo(registerDeviceSignResponse.checkForErrors().get());
    }

    private RegisterDeviceSignResponse mockValidRegisterDeviceSignResponse() {
        RegisterDeviceSignResponse registerDeviceSignResponse =
                mock(RegisterDeviceSignResponse.class);
        when(registerDeviceSignResponse.checkForErrors()).thenReturn(Optional.empty());
        return registerDeviceSignResponse;
    }

    private RegisterDeviceSignResponse mockInvalidRegisterDeviceSignResponse() {
        AgentBankApiError error = mock(AgentBankApiError.class);
        RegisterDeviceSignResponse registerDeviceSignResponse =
                mock(RegisterDeviceSignResponse.class);
        when(registerDeviceSignResponse.checkForErrors()).thenReturn(Optional.of(error));
        return registerDeviceSignResponse;
    }
}
