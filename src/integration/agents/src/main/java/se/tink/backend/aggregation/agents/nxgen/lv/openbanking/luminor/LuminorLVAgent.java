package se.tink.backend.aggregation.agents.nxgen.lv.openbanking.luminor;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorBaseAgent;
import se.tink.backend.aggregation.client.provider_configuration.rpc.Capability;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({Capability.CHECKING_ACCOUNTS, Capability.SAVINGS_ACCOUNTS})
public class LuminorLVAgent extends LuminorBaseAgent {

    @Inject
    public LuminorLVAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }
}
