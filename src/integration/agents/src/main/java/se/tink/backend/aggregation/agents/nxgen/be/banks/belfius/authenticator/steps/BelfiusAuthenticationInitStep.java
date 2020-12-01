package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.steps;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusPersistedDataAccessor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentStartAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@AllArgsConstructor
public class BelfiusAuthenticationInitStep
        implements AgentAuthenticationProcessStep<AgentStartAuthenticationProcessRequest> {

    private final BelfiusDataAccessorFactory dataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(AgentStartAuthenticationProcessRequest request) {
        BelfiusPersistedDataAccessor persistedData =
                dataAccessorFactory.createBelfiusPersistedDataAccessor(
                        request.getAuthenticationPersistedData());
        BelfiusAuthenticationData authenticationData = persistedData.getBelfiusAuthenticationData();

        if (authenticationData.hasCredentials()) {
            return new AgentProceedNextStepAuthenticationResult(
                    AgentAuthenticationProcessStepIdentifier.of(
                            AutoAuthenticationInitStep.class.getSimpleName()),
                    request.getAuthenticationPersistedData());
        }
        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        UsernameAndPasswordGetStep.class.getSimpleName()),
                persistedData.storeBelfiusAuthenticationData(authenticationData));
    }
}
