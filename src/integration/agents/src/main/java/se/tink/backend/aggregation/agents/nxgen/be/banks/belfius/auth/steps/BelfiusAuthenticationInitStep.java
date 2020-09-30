package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.steps;

import java.util.HashMap;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusPersistedData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentStartAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@AllArgsConstructor
public class BelfiusAuthenticationInitStep
        implements AgentAuthenticationProcessStep<AgentStartAuthenticationProcessRequest> {

    private final BelfiusPersistedDataAccessorFactory belfiusPersistedDataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(AgentStartAuthenticationProcessRequest request) {
        BelfiusPersistedData persistedData =
                belfiusPersistedDataAccessorFactory.createBelfiusPersistedDataAccessor(
                        request.getAuthenticationPersistedData());
        BelfiusAuthenticationData authenticationData = persistedData.getBelfiusAuthenticationData();

        AgentAuthenticationProcessState processState = buildEmptyProcessState();

        if (authenticationData.hasCredentials()) {
            return new AgentProceedNextStepAuthenticationResult(
                    AgentAuthenticationProcessStepIdentifier.of(
                            AutoAuthenticationInitStep.class.getSimpleName()),
                    processState,
                    request.getAuthenticationPersistedData());
        }
        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        UsernameAndPasswordGetStep.class.getSimpleName()),
                processState,
                persistedData.storeBelfiusAuthenticationData(authenticationData));
    }

    private AgentAuthenticationProcessState buildEmptyProcessState() {
        HashMap<String, Object> values = new HashMap<>();
        values.put(BelfiusProcessState.KEY, BelfiusProcessState.builder().build());
        return new AgentAuthenticationProcessState(values);
    }
}
