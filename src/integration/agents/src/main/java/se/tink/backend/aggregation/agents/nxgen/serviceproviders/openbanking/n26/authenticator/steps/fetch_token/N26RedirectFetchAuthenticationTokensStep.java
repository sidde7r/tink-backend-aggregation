package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_token;

import agents_platform_agents_framework.lombok.NonNull;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_consent.N26FetchConsentStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentRemoteInteractionAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRedirectTokensAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationAuthorizationResponseErrorVerifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectFetchAuthenticationTokensStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectFetchTokenCall;

public class N26RedirectFetchAuthenticationTokensStep
        extends RedirectFetchAuthenticationTokensStep {

    public N26RedirectFetchAuthenticationTokensStep(
            @NonNull RedirectFetchTokenCall redirectFetchTokenCall,
            @NonNull
                    RedirectAuthenticationAuthorizationResponseErrorVerifier
                            authorizationResponseErrorVerifier,
            AgentRedirectTokensAuthenticationPersistedDataAccessorFactory
                    agentRedirectTokensAuthenticationPersistedDataAccessorFactory) {
        super(
                redirectFetchTokenCall,
                authorizationResponseErrorVerifier,
                agentRedirectTokensAuthenticationPersistedDataAccessorFactory);
    }

    @Override
    public AgentAuthenticationResult execute(
            AgentRemoteInteractionAuthenticationRequest authenticationProcessRequest) {
        AgentAuthenticationResult executeResult = super.execute(authenticationProcessRequest);
        if (executeResult instanceof AgentSucceededAuthenticationResult) {
            return new AgentProceedNextStepAuthenticationResult(
                    AgentAuthenticationProcessStepIdentifier.of(
                            N26FetchConsentStep.class.getSimpleName()),
                    authenticationProcessRequest.getAuthenticationPersistedData());
        }
        return executeResult;
    }
}
