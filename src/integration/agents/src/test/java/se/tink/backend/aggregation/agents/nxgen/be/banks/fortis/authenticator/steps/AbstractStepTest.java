package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import org.junit.Ignore;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.AgentFieldDefinition;

@Ignore
public abstract class AbstractStepTest {

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected void assertStepResult(AgentAuthenticationResult result, Class<?> nextStepClass) {

        if (!result.getAuthenticationProcessStepIdentifier().isPresent()) {
            fail("No step in result");
        }

        assertEquals(
                nextStepClass.getSimpleName(),
                result.getAuthenticationProcessStepIdentifier().get().getValue());

        if (result instanceof AgentProceedNextStepAuthenticationResult) {
            AgentProceedNextStepAuthenticationResult nextStepResult =
                    (AgentProceedNextStepAuthenticationResult) result;

            assertThat(nextStepResult.getAuthenticationProcessState()).isNotNull();
            assertThat(nextStepResult.getAuthenticationPersistedData()).isNotNull();
        } else if (result instanceof AgentUserInteractionDefinitionResult) {
            AgentUserInteractionDefinitionResult nextStepResult =
                    (AgentUserInteractionDefinitionResult) result;

            assertThat(nextStepResult.getAuthenticationProcessState()).isNotNull();
            assertThat(nextStepResult.getAuthenticationPersistedData()).isNotNull();
        }
    }

    protected void assertProcessStateContains(AgentAuthenticationResult result, String key)
            throws IOException {

        Map<String, String> processState = getProcessState(result);
        assertThat(processState.get(key)).isNotNull();
    }

    protected void assertProcessStateContainsValue(
            AgentAuthenticationResult result, String key, String value) throws IOException {

        Map<String, String> processState = getProcessState(result);
        assertThat(processState.get(key)).isNotNull().isEqualTo(value);
    }

    protected void assertAuthDataContainsValue(
            AgentAuthenticationResult result, String key, String value) throws IOException {

        Map<String, String> authData;
        if (result instanceof AgentSucceededAuthenticationResult) {
            AgentSucceededAuthenticationResult successResult =
                    (AgentSucceededAuthenticationResult) result;
            authData =
                    objectMapper.readValue(
                            successResult
                                    .getAuthenticationPersistedData()
                                    .valuesCopy()
                                    .get("FortisAuthData"),
                            Map.class);
        } else if (result instanceof AgentProceedNextStepAuthenticationResult) {
            AgentProceedNextStepAuthenticationResult nextStepResult =
                    (AgentProceedNextStepAuthenticationResult) result;
            authData =
                    objectMapper.readValue(
                            nextStepResult
                                    .getAuthenticationPersistedData()
                                    .valuesCopy()
                                    .get("FortisAuthData"),
                            Map.class);
        } else {
            throw new AssertionError("Unsupported result");
        }

        assertThat(authData.get(key)).isNotNull().isEqualTo(value);
    }

    private Map<String, String> getProcessState(AgentAuthenticationResult result)
            throws IOException {

        Map<String, String> processState;

        if (result instanceof AgentProceedNextStepAuthenticationResult) {
            AgentProceedNextStepAuthenticationResult nextStepResult =
                    (AgentProceedNextStepAuthenticationResult) result;

            processState =
                    objectMapper.readValue(
                            nextStepResult
                                    .getAuthenticationProcessState()
                                    .get()
                                    .get("FortisProcessState"),
                            Map.class);
        } else if (result instanceof AgentUserInteractionDefinitionResult) {
            AgentUserInteractionDefinitionResult nextStepResult =
                    (AgentUserInteractionDefinitionResult) result;

            processState =
                    objectMapper.readValue(
                            nextStepResult
                                    .getAuthenticationProcessState()
                                    .get()
                                    .get("FortisProcessState"),
                            Map.class);
        } else {
            throw new AssertionError("Unsuitable step result type");
        }
        return processState;
    }

    protected void assertFieldRequested(AgentAuthenticationResult result, String fieldKey) {
        AgentUserInteractionDefinitionResult nextStepResult =
                (AgentUserInteractionDefinitionResult) result;

        Optional<AgentFieldDefinition> any =
                nextStepResult.getUserInteractionDefinition().getRequiredFields().stream()
                        .filter(field -> fieldKey.equals(field.getFieldIdentifier()))
                        .findAny();

        if (!any.isPresent()) {
            fail("Field " + fieldKey + " was not requested");
        }
    }
}
