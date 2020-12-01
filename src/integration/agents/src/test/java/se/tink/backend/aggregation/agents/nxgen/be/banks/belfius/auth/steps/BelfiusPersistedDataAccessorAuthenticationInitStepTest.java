package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.AutoAuthenticationInitStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.BelfiusAuthenticationInitStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps.UsernameAndPasswordGetStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentStartAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;

public class BelfiusPersistedDataAccessorAuthenticationInitStepTest extends BaseStep {

    private BelfiusAuthenticationInitStep step =
            new BelfiusAuthenticationInitStep(createBelfiusDataAccessorFactory());

    @Test
    public void shouldStartManualAuthenticationWhenCredentialsAreAbsent() {
        // given
        AgentAuthenticationPersistedData persisted =
                new AgentAuthenticationPersistedData(new HashMap<>());
        AgentStartAuthenticationProcessRequest request =
                new AgentStartAuthenticationProcessRequest(persisted);

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then
        assertThat(result).isInstanceOf(AgentProceedNextStepAuthenticationResult.class);
        AgentProceedNextStepAuthenticationResult nextStepResult =
                (AgentProceedNextStepAuthenticationResult) result;
        assertThat(nextStepResult.getAuthenticationProcessStepIdentifier().get())
                .isEqualTo(
                        AgentAuthenticationProcessStepIdentifier.of(
                                UsernameAndPasswordGetStep.class.getSimpleName()));
    }

    @Test
    public void shouldStartAutoAuthenticationWhenCredentialsArePresent() {
        // given
        BelfiusAuthenticationData belfiusPersistence =
                new BelfiusAuthenticationData()
                        .password("password")
                        .panNumber("panNumber")
                        .deviceToken("deviceToken");
        AgentAuthenticationPersistedData persisted =
                createBelfiusDataAccessorFactory()
                        .createBelfiusPersistedDataAccessor(
                                new AgentAuthenticationPersistedData(new HashMap<>()))
                        .storeBelfiusAuthenticationData(belfiusPersistence);
        AgentStartAuthenticationProcessRequest request =
                new AgentStartAuthenticationProcessRequest(persisted);

        // when
        AgentAuthenticationResult result = step.execute(request);

        // then
        assertThat(result).isInstanceOf(AgentProceedNextStepAuthenticationResult.class);
        AgentProceedNextStepAuthenticationResult nextStepResult =
                (AgentProceedNextStepAuthenticationResult) result;
        assertThat(nextStepResult.getAuthenticationProcessStepIdentifier().get())
                .isEqualTo(
                        AgentAuthenticationProcessStepIdentifier.of(
                                AutoAuthenticationInitStep.class.getSimpleName()));
    }
}
