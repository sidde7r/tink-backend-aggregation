package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.AgentPlatformBelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.AgentFieldDefinition;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.CardReaderLoginDescriptionAgentField;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.CardReaderLoginInputAgentField;

public class RegisterDeviceGetLoginCodeStepTest extends BaseStep {

    @Test
    public void shouldGetChallengeAndAskForLoginCode() {
        // given
        AgentPlatformBelfiusApiClient apiClient = Mockito.mock(AgentPlatformBelfiusApiClient.class);
        RegisterDeviceGetLoginCodeStep step =
                new RegisterDeviceGetLoginCodeStep(apiClient, createBelfiusDataAccessorFactory());

        AgentProceedNextStepAuthenticationRequest request =
                createAgentProceedNextStepAuthenticationRequest(
                        new BelfiusProcessState().sessionId(SESSION_ID).machineId(MACHINE_ID),
                        new BelfiusAuthenticationData().panNumber(PAN_NUMBER));

        when(apiClient.prepareAuthentication(SESSION_ID, MACHINE_ID, "1", PAN_NUMBER))
                .thenReturn(CHALLENGE);

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then
        assertThat(result).isInstanceOf(AgentUserInteractionDefinitionResult.class);
        AgentUserInteractionDefinitionResult nextStepResult =
                (AgentUserInteractionDefinitionResult) result;
        List<AgentFieldDefinition> requiredFields =
                nextStepResult.getUserInteractionDefinition().getRequiredFields();
        assertThat(requiredFields).hasSize(2);
        assertThat(requiredFields)
                .element(0)
                .isInstanceOf(CardReaderLoginDescriptionAgentField.class);
        CardReaderLoginDescriptionAgentField requiredField =
                (CardReaderLoginDescriptionAgentField) requiredFields.get(0);
        assertThat(requiredField.getFieldLabel().getLabel()).isEqualTo(CHALLENGE);
        assertThat(requiredFields).element(1).isInstanceOf(CardReaderLoginInputAgentField.class);
    }
}
