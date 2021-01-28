package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentStartAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@RequiredArgsConstructor
public class LunarAuthenticationInitStep
        implements AgentAuthenticationProcessStep<AgentStartAuthenticationProcessRequest> {

    private final LunarDataAccessorFactory dataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(AgentStartAuthenticationProcessRequest request) {
        LunarAuthDataAccessor persistedData =
                dataAccessorFactory.createAuthDataAccessor(
                        request.getAuthenticationPersistedData());
        LunarAuthData authData = persistedData.get();

        if (authData.hasCredentials()) {
            return new AgentProceedNextStepAuthenticationResult(
                    AgentAuthenticationProcessStepIdentifier.of(
                            AutoAuthenticationStep.class.getSimpleName()),
                    request.getAuthenticationPersistedData());
        }

        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        GetUserCredentialsStep.class.getSimpleName()),
                request.getAuthenticationPersistedData());
    }
}
