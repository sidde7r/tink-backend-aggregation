package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.getcredentials;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.savecredentials.CredentialsSaveStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;

public class CredentialsGetStepTest {

    @Test
    public void shouldInitializeNextStepCredentialsSaveStep() {
        // given
        AgentAuthenticationRequest request =
                new AgentProceedNextStepAuthenticationRequest(null, null, null, null);
        CredentialsGetStep credentialsGetStep = new CredentialsGetStep();

        // when
        AgentAuthenticationResult execute = credentialsGetStep.execute(request);

        // then
        assertThat(execute).isInstanceOf(AgentUserInteractionDefinitionResult.class);
        assertThat(execute.getAuthenticationProcessStepIdentifier().get().getValue())
                .isEqualTo(CredentialsSaveStep.class.getSimpleName());
        AgentUserInteractionDefinitionResult definitionResult =
                (AgentUserInteractionDefinitionResult) execute;
        assertThat(definitionResult.getUserInteractionDefinition().getRequiredFields().size())
                .isEqualTo(3);
    }
}
