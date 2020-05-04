package se.tink.backend.aggregation.agents.module;

import com.google.inject.AbstractModule;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

/** Module binding agent configuration, context, and credentials request. */
public final class AgentRequestScopeModule extends AbstractModule {

    private final CredentialsRequest request;
    private final AgentContext agentContext;
    private final AgentsServiceConfiguration configuration;

    public AgentRequestScopeModule(
            CredentialsRequest request,
            AgentContext agentContext,
            AgentsServiceConfiguration configuration) {

        this.request = request;
        this.agentContext = agentContext;
        this.configuration = configuration;
    }

    @Override
    protected void configure() {

        bind(CredentialsRequest.class).toInstance(request);
        bind(SupplementalRequester.class).toInstance(agentContext);
        bind(AgentContext.class).toInstance(agentContext);
        bind(AgentsServiceConfiguration.class).toInstance(configuration);
        bind(CompositeAgentContext.class).toInstance(agentContext);
        bind(SignatureKeyPair.class).toInstance(configuration.getSignatureKeyPair());
        bind(EidasProxyConfiguration.class).toInstance(configuration.getEidasProxy());
    }
}
