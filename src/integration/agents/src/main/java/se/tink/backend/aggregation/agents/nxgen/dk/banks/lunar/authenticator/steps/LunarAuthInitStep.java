package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentStartAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@RequiredArgsConstructor
public class LunarAuthInitStep
        implements AgentAuthenticationProcessStep<AgentStartAuthenticationProcessRequest> {

    private final LunarDataAccessorFactory dataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(AgentStartAuthenticationProcessRequest request) {
        LunarAuthData authData =
                dataAccessorFactory
                        .createAuthDataAccessor(request.getAuthenticationPersistedData())
                        .get();

        AgentAuthenticationProcessStepIdentifier nextStep =
                authData.hasCredentials()
                        ? AgentAuthenticationProcessStep.identifier(AutoAuthenticationStep.class)
                        : AgentAuthenticationProcessStep.identifier(GetUserCredentialsStep.class);

        return new AgentProceedNextStepAuthenticationResult(
                nextStep, request.getAuthenticationPersistedData());
    }
}
