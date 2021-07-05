package se.tink.backend.aggregation.agents.nxgen.lt.openbanking.luminor;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorBaseAgent;
import se.tink.backend.aggregation.client.provider_configuration.rpc.Capability;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({
    Capability.CHECKING_ACCOUNTS,
    Capability.SAVINGS_ACCOUNTS,
    Capability.TRANSFERS
})
public class LuminorLTAgent extends LuminorBaseAgent {

    @Inject
    public LuminorLTAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }
}
