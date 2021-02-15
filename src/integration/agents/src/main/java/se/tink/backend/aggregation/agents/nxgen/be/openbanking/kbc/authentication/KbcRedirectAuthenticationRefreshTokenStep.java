package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationRefreshTokenStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectRefreshTokenCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.RefreshableAccessTokenValidator;

public class KbcRedirectAuthenticationRefreshTokenStep
        extends RedirectAuthenticationRefreshTokenStep {

    public KbcRedirectAuthenticationRefreshTokenStep(
            RedirectRefreshTokenCall redirectRefreshTokenCall,
            AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory
                    agentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory,
            RefreshableAccessTokenValidator tokensValidator) {
        super(
                redirectRefreshTokenCall,
                agentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory,
                tokensValidator);
    }

    @Override
    public AgentAuthenticationResult execute(
            AgentProceedNextStepAuthenticationRequest authenticationProcessRequest) {
        AgentAuthenticationResult authenticationResult =
                super.execute(authenticationProcessRequest);
        if (authenticationResult instanceof AgentSucceededAuthenticationResult) {
            return new AgentProceedNextStepAuthenticationResult(
                    AgentAuthenticationProcessStep.identifier(KbcConsentValidationStep.class),
                    authenticationProcessRequest.getAuthenticationPersistedData());
        }
        return authenticationResult;
    }
}
