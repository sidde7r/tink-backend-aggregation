package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.NorwegianBaseAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({SAVINGS_ACCOUNTS, CREDIT_CARDS})
public class NorwegianAgent extends NorwegianBaseAgent {

    @Inject
    public NorwegianAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, new NorwegianNOConfiguration());
    }
}
