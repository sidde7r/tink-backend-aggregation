package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.RegisterDeviceGetSignCodeStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.RegisterDeviceSignStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.AgentFieldDefinition;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.CardReaderSignDescriptionAgentField;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.CardReaderSignInputAgentField;

public class RegisterDeviceGetSignCodeStepTest extends BaseStep {

    @Test
    public void shouldGetChallengeAndAskForLoginSign() {
        // given
        AgentPlatformBelfiusApiClient apiClient = Mockito.mock(AgentPlatformBelfiusApiClient.class);
        RegisterDeviceGetSignCodeStep step =
                new RegisterDeviceGetSignCodeStep(apiClient, createBelfiusDataAccessorFactory());

        AgentProceedNextStepAuthenticationRequest request =
                createAgentProceedNextStepAuthenticationRequest(
                        new BelfiusProcessState()
                                .sessionId(SESSION_ID)
                                .machineId(MACHINE_ID)
                                .deviceToken(DEVICE_TOKEN),
                        new BelfiusAuthenticationData());

        when(apiClient.prepareDeviceRegistration(
                        eq(SESSION_ID),
                        eq(MACHINE_ID),
                        eq("1"),
                        eq(DEVICE_TOKEN),
                        eq(BelfiusConstants.BRAND),
                        any()))
                .thenReturn(CHALLENGE);

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then

        verify(apiClient).consultClientSettings(SESSION_ID, MACHINE_ID, "1");

        assertThat(result).isInstanceOf(AgentUserInteractionDefinitionResult.class);
        AgentUserInteractionDefinitionResult nextStepResult =
                (AgentUserInteractionDefinitionResult) result;

        assertThat(nextStepResult.getAuthenticationProcessStepIdentifier().get())
                .isEqualTo(
                        AgentAuthenticationProcessStepIdentifier.of(
                                RegisterDeviceSignStep.class.getSimpleName()));

        List<AgentFieldDefinition> requiredFields =
                nextStepResult.getUserInteractionDefinition().getRequiredFields();
        assertThat(requiredFields).hasSize(2);
        assertThat(requiredFields)
                .element(0)
                .isInstanceOf(CardReaderSignDescriptionAgentField.class);
        CardReaderSignDescriptionAgentField requiredField =
                (CardReaderSignDescriptionAgentField) requiredFields.get(0);
        assertThat(requiredField.getFieldLabel().getLabel()).isEqualTo(CHALLENGE);
        assertThat(requiredFields).element(1).isInstanceOf(CardReaderSignInputAgentField.class);
    }
}
