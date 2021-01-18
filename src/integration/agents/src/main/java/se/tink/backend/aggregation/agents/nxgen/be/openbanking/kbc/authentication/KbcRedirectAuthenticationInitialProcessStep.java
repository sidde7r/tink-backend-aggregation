package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.IbanAgentUserInteractionAuthenticationStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationInitialProcessStep;

public class KbcRedirectAuthenticationInitialProcessStep
        extends RedirectAuthenticationInitialProcessStep {

    public KbcRedirectAuthenticationInitialProcessStep(
            AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory
                    agentRedirectTokensAuthenticationPersistedDataAccessorFactory) {
        super(agentRedirectTokensAuthenticationPersistedDataAccessorFactory);
    }

    @Override
    protected AgentAuthenticationProcessStepIdentifier
            getManualAuthenticationBeginStepIdentifier() {
        return AgentAuthenticationProcessStep.identifier(
                IbanAgentUserInteractionAuthenticationStep.class);
    }
}
