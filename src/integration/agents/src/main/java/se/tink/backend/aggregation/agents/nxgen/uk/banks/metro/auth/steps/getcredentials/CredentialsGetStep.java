package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.getcredentials;

import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.AgentField;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.savecredentials.CredentialsSaveStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

public class CredentialsGetStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        return new AgentUserInteractionDefinitionResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        CredentialsSaveStep.class.getSimpleName()),
                request.getAuthenticationPersistedData(),
                request.getAuthenticationProcessState(),
                new AgentField(Key.USERNAME.getFieldKey(), "username"),
                new AgentField(Key.PASSWORD.getFieldKey(), "password"),
                new AgentField(Key.SECURITY_NUMBER.getFieldKey(), "security-number"));
    }
}
