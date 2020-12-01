package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.fields.AgentField;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@AllArgsConstructor
@Slf4j
public class UsernameAndPasswordGetStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        return new AgentUserInteractionDefinitionResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        UsernameAndPasswordSaveStep.class.getSimpleName()),
                request.getAuthenticationPersistedData(),
                request.getAuthenticationProcessState(),
                new AgentField(Key.USERNAME.getFieldKey(), "username"),
                new AgentField(Key.PASSWORD.getFieldKey(), "password"));
    }
}
