package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module;

import com.google.inject.Inject;
import com.google.inject.Provider;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.module.agentclass.AgentClass;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.toggle.UkOpenBankingFlow;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.toggle.UkOpenBankingFlowToggle;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class UkOpenBankingFlowFactory implements Provider<UkOpenBankingFlowFacade> {

    private final UkOpenBankingFlowToggle ukOpenBankingFlowToggle;
    private final CompositeAgentContext context;
    private final AgentComponentProvider agentComponentProvider;
    private final AgentsServiceConfiguration agentsServiceConfiguration;
    private final Class<? extends Agent> agentClass;

    @Inject
    public UkOpenBankingFlowFactory(
            UkOpenBankingFlowToggle ukOpenBankingFlowToggle,
            CompositeAgentContext context,
            AgentComponentProvider agentComponentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration,
            @AgentClass Class<? extends Agent> agentClass) {
        this.ukOpenBankingFlowToggle = ukOpenBankingFlowToggle;
        this.context = context;
        this.agentComponentProvider = agentComponentProvider;
        this.agentsServiceConfiguration = agentsServiceConfiguration;
        this.agentClass = agentClass;
    }

    @Override
    public UkOpenBankingFlowFacade get() {
        UkOpenBankingFlowStrategy flow =
                ukOpenBankingFlowToggle.takeFlow() == UkOpenBankingFlow.EIDAS_PROXY
                        ? new EidasProxyFlow(
                                context,
                                agentComponentProvider,
                                agentsServiceConfiguration,
                                agentClass)
                        : new SecretServiceFlow(context, agentClass);
        return flow.get();
    }
}
