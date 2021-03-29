package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.atlanticoeuropa;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsProgressiveBaseAgent;
import se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, TRANSFERS})
@AgentPisCapability(capabilities = {PisCapability.PIS_SEPA, PisCapability.PIS_FUTURE_DATE})
public final class AtlanticoEuropaAgent extends SibsProgressiveBaseAgent {

    @Inject
    public AtlanticoEuropaAgent(
            AgentComponentProvider agentComponentProvider,
            AgentsServiceConfiguration configuration) {
        super(agentComponentProvider, configuration);
    }
}
