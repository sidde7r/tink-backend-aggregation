package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.fields.AgentField;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.AgentFieldDefinition;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.AgentUsernameFieldDefinition;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;

public class ClientCredentialsGetStepTest extends AbstractStepTest {

    private ClientCredentialsGetStep step;

    @Before
    public void setup() {
        step = new ClientCredentialsGetStep();
    }

    @Test
    public void shouldRequestBasicCredentials() {
        // given
        AgentProceedNextStepAuthenticationRequest request =
                new AgentProceedNextStepAuthenticationRequest(
                        AgentAuthenticationProcessStepIdentifier.of("doesnt-matter"),
                        AgentAuthenticationProcessState.of("FortisProcessState", "{}"),
                        AgentAuthenticationPersistedData.of("FortisAuthData", "{}"),
                        AgentExtendedClientInfo.builder().build());

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then
        assertStepResult(result, ClientCredentialsSaveStep.class);

        assertThat(result).isInstanceOf(AgentUserInteractionDefinitionResult.class);

        AgentUserInteractionDefinitionResult userInteractionDefinitionResult =
                (AgentUserInteractionDefinitionResult) result;

        List<AgentFieldDefinition> fields =
                userInteractionDefinitionResult.getUserInteractionDefinition().getRequiredFields();

        String username = AgentUsernameFieldDefinition.of().getFieldIdentifier();
        String clientNo = AgentField.clientnumber().getFieldIdentifier();

        assertThat(
                        fields.stream()
                                .filter(field -> username.equals(field.getFieldIdentifier()))
                                .count())
                .isEqualTo(1L);
        assertThat(
                        fields.stream()
                                .filter(field -> clientNo.equals(field.getFieldIdentifier()))
                                .count())
                .isEqualTo(1L);
    }
}
