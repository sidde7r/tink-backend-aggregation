package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.citadele;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, IDENTITY_DATA})
public class CitadeleEEAgent extends CitadeleBaseAgent {

    @Inject
    protected CitadeleEEAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    public String getMarketLanguage() {
        return "ET";
    }
}
