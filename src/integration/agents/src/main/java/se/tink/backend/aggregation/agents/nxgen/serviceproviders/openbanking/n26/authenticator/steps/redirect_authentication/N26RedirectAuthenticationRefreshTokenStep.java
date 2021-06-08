package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.redirect_authentication;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent.N26AutoAuthValidateConsentStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.error.AuthenticationStepBankSiteErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.error.N26BankSiteErrorDiscoverer;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationRefreshTokenStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectRefreshTokenCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.RefreshableAccessTokenValidator;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.SessionExpiredError;

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
                new AuthenticationStepBankSiteErrorHandler<
                        AgentProceedNextStepAuthenticationRequest>(
                        new N26BankSiteErrorDiscoverer()) {
                    protected AgentAuthenticationResult execute(
                            AgentProceedNextStepAuthenticationRequest
                                    authenticationProcessRequest) {
                        return N26RedirectAuthenticationRefreshTokenStep.super.execute(
                                authenticationProcessRequest);
                    }
                }.executeWithHandling(authenticationProcessRequest);
        if (authenticationResult instanceof AgentSucceededAuthenticationResult) {
            return new AgentProceedNextStepAuthenticationResult(
                    AgentAuthenticationProcessStep.identifier(N26AutoAuthValidateConsentStep.class),
                    authenticationResult.getAuthenticationPersistedData());
        } else if (authenticationResult instanceof AgentProceedNextStepAuthenticationResult) {
            /**
             * When the flow of super method goes to the {@link #prepareNewAuthenticationStepResult}
             * instead of going to manual flow we want to throw return SessionExpiredError
             */
            log.info("SessionExpired: Could not refresh token");
            return new AgentFailedAuthenticationResult(new SessionExpiredError(), null);
        }
        log.error("Unknown state after refreshing access token");
        throw new IllegalStateException(
                "Unknown state after refreshing access token. RedirectAuthenticationRefreshTokenStep has returned unknown state");
    }
}
