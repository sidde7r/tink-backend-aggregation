package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth;

import java.util.List;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAbstractMultiStepsAuthenticationProcess;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentStartAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@AllArgsConstructor
class MetroAuthenticationProcessFacade extends AgentAbstractMultiStepsAuthenticationProcess {

    private final List<AgentAuthenticationProcessStep<?>> processStepList;

    private final AgentAuthenticationProcessStep<AgentStartAuthenticationProcessRequest>
            initialStep;

    @Override
    public void registerSteps() {
        processStepList.forEach(this::addStep);
    }

    @Override
    public AgentAuthenticationProcessStep<AgentStartAuthenticationProcessRequest> getStartStep() {
        return initialStep;
    }
}
