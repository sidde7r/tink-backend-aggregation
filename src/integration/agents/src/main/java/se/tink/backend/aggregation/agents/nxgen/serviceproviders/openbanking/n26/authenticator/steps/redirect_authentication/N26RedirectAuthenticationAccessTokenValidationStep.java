package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.redirect_authentication;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent.N26AutoAuthValidateConsentStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRedirectTokensAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationAccessTokenValidationStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationRefreshTokenStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectTokensValidator;

@Slf4j
public class N26RedirectAuthenticationAccessTokenValidationStep
        extends RedirectAuthenticationAccessTokenValidationStep {

    public N26RedirectAuthenticationAccessTokenValidationStep(
            AgentRedirectTokensAuthenticationPersistedDataAccessorFactory
                    agentRedirectTokensAuthenticationPersistedDataAccessorFactory,
            RedirectTokensValidator tokenValidator) {
        super(agentRedirectTokensAuthenticationPersistedDataAccessorFactory, tokenValidator);
    }

    @Override
    public AgentAuthenticationResult execute(
            AgentProceedNextStepAuthenticationRequest authenticationProcessRequest) {
        AgentAuthenticationResult result = super.execute(authenticationProcessRequest);

        if (result instanceof AgentSucceededAuthenticationResult) {
            return new AgentProceedNextStepAuthenticationResult(
                    AgentAuthenticationProcessStep.identifier(N26AutoAuthValidateConsentStep.class),
                    authenticationProcessRequest.getAuthenticationPersistedData(),
                    authenticationProcessRequest.getAgentExtendedClientInfo());
        } else if (result instanceof AgentProceedNextStepAuthenticationResult) {
            return new AgentProceedNextStepAuthenticationResult(
                    AgentAuthenticationProcessStep.identifier(
                            RedirectAuthenticationRefreshTokenStep.class),
                    authenticationProcessRequest.getAuthenticationPersistedData(),
                    authenticationProcessRequest.getAgentExtendedClientInfo());
        }
        log.error("Unknown state after validating access token");
        throw new IllegalStateException(
                "Unknown state after validating access token. RedirectAuthenticationAccessTokenValidationStep has returned unknown state");
    }
}
