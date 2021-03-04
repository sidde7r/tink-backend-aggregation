package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarProcessState;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarProcessStateAccessor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@Slf4j
@RequiredArgsConstructor
public class AutoAuthenticationStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    private final LunarDataAccessorFactory dataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        LunarProcessStateAccessor processStateAccessor =
                dataAccessorFactory.createProcessStateAccessor(
                        request.getAuthenticationProcessState());
        LunarProcessState processState = processStateAccessor.get();

        processState.setAutoAuth(true);

        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStep.identifier(FetchAccountsToConfirmLoginStep.class),
                processStateAccessor.storeState(processState),
                request.getAuthenticationPersistedData());
    }
}
