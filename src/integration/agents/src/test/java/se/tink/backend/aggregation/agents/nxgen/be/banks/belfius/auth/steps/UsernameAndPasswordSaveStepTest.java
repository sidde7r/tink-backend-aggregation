package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.utils.BelfiusStringUtils;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.AgentFieldValue;

public class UsernameAndPasswordSaveStepTest extends BaseStepTest {

    @Test
    public void shouldSaveUsernameAndPassword() {
        // given
        UsernameAndPasswordSaveStep step =
                new UsernameAndPasswordSaveStep(createBelfiusPersistedDataAccessorFactory());

        AgentUserInteractionAuthenticationProcessRequest request =
                createAgentUserInteractionAuthenticationProcessRequest(
                        BelfiusProcessState.builder().build(),
                        new BelfiusAuthenticationData(),
                        new AgentFieldValue(Key.PASSWORD.getFieldKey(), PASSWORD),
                        new AgentFieldValue(Key.USERNAME.getFieldKey(), PAN_NUMBER));

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then

        assertThat(result).isInstanceOf(AgentProceedNextStepAuthenticationResult.class);
        AgentProceedNextStepAuthenticationResult nextStepResult =
                (AgentProceedNextStepAuthenticationResult) result;

        assertThat(nextStepResult.getAuthenticationProcessStepIdentifier().get())
                .isEqualTo(
                        AgentAuthenticationProcessStepIdentifier.of(
                                ManualAuthenticationInitStep.class.getSimpleName()));

        BelfiusAuthenticationData persistence =
                createBelfiusPersistedDataAccessorFactory()
                        .createBelfiusPersistedDataAccessor(
                                nextStepResult.getAuthenticationPersistedData())
                        .getBelfiusAuthenticationData();
        assertThat(nextStepResult.getAuthenticationProcessState()).isPresent();
        assertThat(persistence.getPassword()).isEqualTo(PASSWORD);
        assertThat(persistence.getPanNumber())
                .isEqualTo(BelfiusStringUtils.formatPanNumber(PAN_NUMBER));
    }
}
