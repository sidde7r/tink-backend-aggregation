package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.caixacrl;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsProgressiveBaseAgent;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class CaixaCrlAgent extends SibsProgressiveBaseAgent {

    @Inject
    public CaixaCrlAgent(
            AgentComponentProvider agentComponentProvider,
            AgentsServiceConfiguration configuration) {
        super(agentComponentProvider, configuration);
    }
}
