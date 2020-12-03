package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisAuthData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentStartAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;

@RequiredArgsConstructor
public class AuthInitStep
        implements AgentAuthenticationProcessStep<AgentStartAuthenticationProcessRequest> {

    private static final Logger LOG = LoggerFactory.getLogger(AuthInitStep.class);

    private final FortisDataAccessorFactory dataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(AgentStartAuthenticationProcessRequest request) {
        FortisAuthDataAccessor persistedData =
                dataAccessorFactory.createAuthDataAccessor(
                        request.getAuthenticationPersistedData());
        FortisAuthData authenticationData = persistedData.get();

        String nextStepName;
        if (authenticationData.hasLegacyCredentials()) {
            LOG.info("User uses legacy flow");
            nextStepName = LegacyAutoAuthStep.class.getSimpleName();
        } else if (authenticationData.hasCredentials()) {
            nextStepName = AutoAuthStep.class.getSimpleName();
        } else {
            nextStepName = ClientCredentialsGetStep.class.getSimpleName();
        }

        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(nextStepName),
                request.getAuthenticationPersistedData());
    }
}
