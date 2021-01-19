package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.redirect_authentication;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_authorization_url.N26FetchAuthorizationUrlStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent.N26AutoAuthValidateConsentStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationRefreshTokenStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectRefreshTokenCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.RefreshableAccessTokenValidator;

@Slf4j
public class N26RedirectAuthenticationRefreshTokenStep
        extends RedirectAuthenticationRefreshTokenStep {

    public N26RedirectAuthenticationRefreshTokenStep(
            RedirectRefreshTokenCall redirectRefreshTokenCall,
            AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory
                    agentRedirectTokensAuthenticationPersistedDataAccessorFactory,
            RefreshableAccessTokenValidator tokensValidator) {
        super(
                redirectRefreshTokenCall,
                agentRedirectTokensAuthenticationPersistedDataAccessorFactory,
                tokensValidator);
    }

    @Override
    public AgentAuthenticationResult execute(
            AgentProceedNextStepAuthenticationRequest authenticationProcessRequest) {
        AgentAuthenticationResult authenticationResult =
                super.execute(authenticationProcessRequest);
        if (authenticationResult instanceof AgentSucceededAuthenticationResult) {
            return new AgentProceedNextStepAuthenticationResult(
                    AgentAuthenticationProcessStep.identifier(N26AutoAuthValidateConsentStep.class),
                    authenticationProcessRequest.getAuthenticationPersistedData());
        } else if (authenticationResult instanceof AgentProceedNextStepAuthenticationResult) {
            return new AgentProceedNextStepAuthenticationResult(
                    AgentAuthenticationProcessStepIdentifier.of(
                            N26FetchAuthorizationUrlStep.class.getSimpleName()),
                    authenticationProcessRequest.getAuthenticationPersistedData());
        }
        log.error("Unknown state after refreshing access token");
        throw new IllegalStateException(
                "Unknown state after refreshing access token. RedirectAuthenticationRefreshTokenStep has returned unknown state");
    }
}
