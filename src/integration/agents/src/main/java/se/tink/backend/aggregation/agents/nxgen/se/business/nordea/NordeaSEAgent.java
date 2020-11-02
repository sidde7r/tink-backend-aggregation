package se.tink.backend.aggregation.agents.nxgen.se.business.nordea;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.filter.NordeaSEFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, IDENTITY_DATA})
public final class NordeaSEAgent extends NordeaBaseAgent {
    @Inject
    public NordeaSEAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, new NordeaSEConfiguration());
        client.addFilter(new NordeaSEFilter());
    }
}
