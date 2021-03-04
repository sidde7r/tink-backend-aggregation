package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps;

import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.AgentPasswordFieldDefinition;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.AgentUsernameFieldDefinition;

public class GetUserCredentialsStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        return new AgentUserInteractionDefinitionResult(
                AgentAuthenticationProcessStep.identifier(SaveUserCredentialsStep.class),
                request.getAuthenticationPersistedData(),
                request.getAuthenticationProcessState(),
                AgentUsernameFieldDefinition.of(),
                AgentPasswordFieldDefinition.of());
    }
}
