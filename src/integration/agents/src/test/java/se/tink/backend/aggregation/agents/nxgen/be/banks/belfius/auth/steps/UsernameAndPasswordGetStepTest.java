package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.fields.AgentField;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.AgentFieldDefinition;

public class UsernameAndPasswordGetStepTest extends BaseStepTest {

    @Test
    public void shouldAskForUsernameAndPassword() {
        // given
        UsernameAndPasswordGetStep step = new UsernameAndPasswordGetStep();

        AgentProceedNextStepAuthenticationRequest request =
                createAgentProceedNextStepAuthenticationRequest(
                        BelfiusProcessState.builder().build(), new BelfiusAuthenticationData());

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then

        assertThat(result).isInstanceOf(AgentUserInteractionDefinitionResult.class);
        AgentUserInteractionDefinitionResult nextStepResult =
                (AgentUserInteractionDefinitionResult) result;

        assertThat(nextStepResult.getAuthenticationProcessStepIdentifier().get())
                .isEqualTo(
                        AgentAuthenticationProcessStepIdentifier.of(
                                UsernameAndPasswordSaveStep.class.getSimpleName()));

        List<AgentFieldDefinition> requiredFields =
                nextStepResult.getUserInteractionDefinition().getRequiredFields();
        assertThat(requiredFields).hasSize(2);
        assertThat(requiredFields).element(0).isInstanceOf(AgentField.class);
        AgentField requiredField = (AgentField) requiredFields.get(0);
        assertThat(requiredField.getFieldLabel().getLabel()).isEqualTo("username");
        AgentField requiredField1 = (AgentField) requiredFields.get(1);
        assertThat(requiredField1.getFieldLabel().getLabel()).isEqualTo("password");
    }
}
