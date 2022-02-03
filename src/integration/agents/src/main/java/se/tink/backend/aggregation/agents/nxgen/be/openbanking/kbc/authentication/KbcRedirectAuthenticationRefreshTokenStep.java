package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationRefreshTokenStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectRefreshTokenCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.RefreshableAccessTokenValidator;

public class KbcRedirectAuthenticationRefreshTokenStep
        extends RedirectAuthenticationRefreshTokenStep {

    private final boolean userAvailableForInteraction;

    public KbcRedirectAuthenticationRefreshTokenStep(
            RedirectRefreshTokenCall redirectRefreshTokenCall,
            AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory
                    agentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory,
            RefreshableAccessTokenValidator tokensValidator,
            boolean userAvailableForInteraction) {
        super(
                redirectRefreshTokenCall,
                agentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory,
                tokensValidator);
        this.userAvailableForInteraction = userAvailableForInteraction;
    }

    @Override
    public AgentAuthenticationResult execute(
            AgentProceedNextStepAuthenticationRequest authenticationProcessRequest) {
        AgentAuthenticationResult authenticationResult =
                super.execute(authenticationProcessRequest);

        return new KbcRedirectAuthenticationRefreshTokenStepHandler()
                .defineResultOfAccessTokenRefresh(
                        authenticationProcessRequest,
                        authenticationResult,
                        userAvailableForInteraction);
    }
}
