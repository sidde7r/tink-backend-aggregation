package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps;

import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.LogTags.LUNAR_TAG;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentStartAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@RequiredArgsConstructor
@Slf4j
public class LunarAuthInitStep
        implements AgentAuthenticationProcessStep<AgentStartAuthenticationProcessRequest> {

    private final LunarDataAccessorFactory dataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(AgentStartAuthenticationProcessRequest request) {
        log.info("{} Entering LunarAuthInitStep", LUNAR_TAG);

        LunarAuthData authData =
                dataAccessorFactory
                        .createAuthDataAccessor(request.getAuthenticationPersistedData())
                        .getData();

        AgentAuthenticationProcessStepIdentifier nextStep =
                authData.hasCredentials()
                        ? AgentAuthenticationProcessStep.identifier(AutoAuthenticationStep.class)
                        : AgentAuthenticationProcessStep.identifier(GetUserCredentialsStep.class);

        return new AgentProceedNextStepAuthenticationResult(
                nextStep, request.getAuthenticationPersistedData());
    }
}
