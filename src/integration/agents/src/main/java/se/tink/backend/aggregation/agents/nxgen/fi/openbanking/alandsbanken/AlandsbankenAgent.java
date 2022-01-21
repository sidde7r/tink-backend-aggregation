package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.alandsbanken;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration.CrosskeyMarketConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, CREDIT_CARDS})
public final class AlandsbankenAgent extends CrosskeyBaseAgent {

    private static final CrosskeyMarketConfiguration ALANDSBANKEN_FI_CONFIGURATION =
            new CrosskeyMarketConfiguration(
                    "alandsbanken-fi",
                    "https://api.alandsbanken.fi",
                    "https://open.alandsbanken.fi");

    @Inject
    public AlandsbankenAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, ALANDSBANKEN_FI_CONFIGURATION);
    }
}
