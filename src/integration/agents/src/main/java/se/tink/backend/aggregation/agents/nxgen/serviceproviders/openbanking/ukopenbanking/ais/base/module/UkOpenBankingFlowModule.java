package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.module.agentclass.AgentClass;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;

public final class UkOpenBankingFlowModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UkOpenBankingFlowFacade.class).toProvider(EidasProxyFlow.class).in(Scopes.SINGLETON);
    }

    @Inject
    @Provides
    @Singleton
    public EidasIdentity eidasIdentity(
            CompositeAgentContext context, @AgentClass Class<? extends Agent> agentClass) {
        return new EidasIdentity(context.getClusterId(), context.getAppId(), "UKOB", agentClass);
    }
}
