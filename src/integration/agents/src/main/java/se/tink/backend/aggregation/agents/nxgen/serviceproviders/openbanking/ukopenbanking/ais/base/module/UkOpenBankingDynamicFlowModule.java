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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.toggle.UkOpenBankingToggleModule;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;

public final class UkOpenBankingDynamicFlowModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new UkOpenBankingToggleModule());
        bind(UkOpenBankingFlowFacade.class)
                .toProvider(UkOpenBankingFlowFactory.class)
                .in(Scopes.SINGLETON);
    }

    @Inject
    @Provides
    @Singleton
    public EidasIdentity eidasIdentity(
            CompositeAgentContext context, @AgentClass Class<? extends Agent> agentClass) {
        // TODO DELETE THIS AFTER TEST !!!!!!!!!!!
        if (context.getAppId().contains("83f56ceed4c94792bfc0532acde5e713")) {
            return new EidasIdentity(
                    context.getClusterId(), context.getAppId(), "ukob", agentClass);
        }
        return new EidasIdentity(context.getClusterId(), context.getAppId(), "UKOB", agentClass);
    }
}
