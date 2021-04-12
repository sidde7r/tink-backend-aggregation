package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module;

import com.google.inject.Inject;
import com.google.inject.Provider;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.toggle.UkOpenBankingFlow;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.toggle.UkOpenBankingFlowToggle;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class UkOpenBankingFlowFactory implements Provider<UkOpenBankingFlowFacade> {

    private final UkOpenBankingFlowToggle ukOpenBankingFlowToggle;
    private final CompositeAgentContext context;
    private final AgentComponentProvider agentComponentProvider;
    private final AgentsServiceConfiguration agentsServiceConfiguration;
    private final EidasIdentity eidasIdentity;

    @Inject
    public UkOpenBankingFlowFactory(
            UkOpenBankingFlowToggle ukOpenBankingFlowToggle,
            CompositeAgentContext context,
            AgentComponentProvider agentComponentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration,
            EidasIdentity eidasIdentity) {
        this.ukOpenBankingFlowToggle = ukOpenBankingFlowToggle;
        this.context = context;
        this.agentComponentProvider = agentComponentProvider;
        this.agentsServiceConfiguration = agentsServiceConfiguration;
        this.eidasIdentity = eidasIdentity;
    }

    @Override
    public UkOpenBankingFlowFacade get() {
        UkOpenBankingFlowStrategy flow =
                ukOpenBankingFlowToggle.takeFlow() == UkOpenBankingFlow.EIDAS_PROXY
                        ? new EidasProxyFlow(
                                context,
                                agentComponentProvider,
                                agentsServiceConfiguration,
                                eidasIdentity)
                        : new SecretServiceFlow(context, eidasIdentity);
        return flow.get();
    }
}
