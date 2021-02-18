package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc;

import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.KbcConsentValidationStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationAccessTokenValidationStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.RefreshableAccessTokenValidator;

public class KbcRedirectAuthenticationAccessTokenValidationStep
        extends RedirectAuthenticationAccessTokenValidationStep {

    public KbcRedirectAuthenticationAccessTokenValidationStep(
            AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory
                    agentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory,
            RefreshableAccessTokenValidator tokenValidator) {
        super(
                agentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory,
                tokenValidator);
    }

    @Override
    public AgentAuthenticationResult execute(
            AgentAuthenticationRequest authenticationProcessRequest) {
        AgentAuthenticationResult authenticationResult =
                super.execute(authenticationProcessRequest);
        if (authenticationResult instanceof AgentSucceededAuthenticationResult) {
            return new AgentProceedNextStepAuthenticationResult(
                    AgentAuthenticationProcessStepIdentifier.of(
                            KbcConsentValidationStep.class.getSimpleName()),
                    authenticationProcessRequest.getAuthenticationProcessState(),
                    authenticationProcessRequest.getAuthenticationPersistedData());
        }
        return authenticationResult;
    }
}
