package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.module.agentclass.AgentClass;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public final class UkOpenBankingEUFlowModule extends AbstractModule {

    @Inject
    @Singleton
    @Provides
    public UkOpenBankingFlowFacade ukOpenBankingFlowFacade(
            CompositeAgentContext context,
            AgentComponentProvider agentComponentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration,
            EidasIdentity eidasIdentity) {
        return new EidasProxyFlow(
                        context, agentComponentProvider, agentsServiceConfiguration, eidasIdentity)
                .get();
    }

    @Inject
    @Provides
    @Singleton
    public EidasIdentity eidasIdentity(
            CompositeAgentContext context, @AgentClass Class<? extends Agent> agentClass) {
        return new EidasIdentity(context.getClusterId(), context.getAppId(), "DEFAULT", agentClass);
    }
}
