package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps;

import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.fields.AgentField;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.AgentPasswordFieldDefinition;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.AgentUsernameFieldDefinition;

@RequiredArgsConstructor
public class GetUserCredentialsStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        return new AgentUserInteractionDefinitionResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        SaveUserCredentialsStep.class.getSimpleName()),
                request.getAuthenticationPersistedData(),
                request.getAuthenticationProcessState(),
                AgentUsernameFieldDefinition.of(),
                AgentPasswordFieldDefinition.of(),
                new AgentField(Field.Key.ACCESS_PIN.getFieldKey(), Storage.ACCESS_PIN_INPUT_LABEL));
    }
}
