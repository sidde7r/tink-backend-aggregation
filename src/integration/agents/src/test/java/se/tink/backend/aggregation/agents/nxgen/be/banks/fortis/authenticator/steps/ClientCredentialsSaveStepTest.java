package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.AgentFieldValue;

public class ClientCredentialsSaveStepTest extends AbstractStepTest {

    private ClientCredentialsSaveStep step;

    @Before
    public void setup() {
        step = new ClientCredentialsSaveStep(new FortisDataAccessorFactory(objectMapper));
    }

    @Test
    public void shouldSaveCredentials() throws Exception {
        // given
        List<AgentFieldValue> fields = new ArrayList<>();
        fields.add(new AgentFieldValue("username", "67030416123412340"));
        fields.add(new AgentFieldValue("clientnumber", "1234012340"));

        AgentUserInteractionAuthenticationProcessRequest request =
                new AgentUserInteractionAuthenticationProcessRequest(
                        AgentAuthenticationProcessStepIdentifier.of("doesnt-matter"),
                        AgentAuthenticationPersistedData.of("FortisAuthData", "{}"),
                        AgentAuthenticationProcessState.of("FortisProcessState", "{}"),
                        fields);

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then
        assertStepResult(result, IdentAuthStep.class);
        assertAuthDataContainsValue(result, "username", "67030416123412340");
        assertAuthDataContainsValue(result, "clientNumber", "1234012340");
    }
}
