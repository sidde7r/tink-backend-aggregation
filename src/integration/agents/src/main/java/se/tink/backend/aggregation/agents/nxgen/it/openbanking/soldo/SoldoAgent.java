package se.tink.backend.aggregation.agents.nxgen.it.openbanking.soldo;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class SoldoAgent extends FabricAgent {
    private static final String BASE_URL = "https://psdgw-soldo.fabrick.com";

    @Inject
    public SoldoAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL;
    }
}
