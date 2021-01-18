package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.authentication_init;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_authorization_url.N26FetchAuthorizationUrlStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRedirectTokensAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationAccessTokenValidationStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationInitialProcessStep;

public class N26AuthenticationInitStep extends RedirectAuthenticationInitialProcessStep {

    public N26AuthenticationInitStep(
            AgentRedirectTokensAuthenticationPersistedDataAccessorFactory
                    agentRedirectTokensAuthenticationPersistedDataAccessorFactory) {
        super(agentRedirectTokensAuthenticationPersistedDataAccessorFactory);
    }

    @Override
    protected AgentAuthenticationProcessStepIdentifier
            getManualAuthenticationBeginStepIdentifier() {
        return AgentAuthenticationProcessStepIdentifier.of(
                N26FetchAuthorizationUrlStep.class.getSimpleName());
    }

    @Override
    protected AgentAuthenticationProcessStepIdentifier getAutoAuthenticationBeginStepIdentifier() {
        return AgentAuthenticationProcessStepIdentifier.of(
                RedirectAuthenticationAccessTokenValidationStep.class.getSimpleName());
    }
}
