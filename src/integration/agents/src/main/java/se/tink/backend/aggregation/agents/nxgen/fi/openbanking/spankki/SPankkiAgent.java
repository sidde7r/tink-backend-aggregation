package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.spankki;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration.CrosskeyMarketConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, CREDIT_CARDS})
public final class SPankkiAgent extends CrosskeyBaseAgent {

    private static final CrosskeyMarketConfiguration SPANKKI_CONFIGURATION =
            new CrosskeyMarketConfiguration(
                    "s-pankki", "https://api.s-pankki.fi", "https://openbanking.s-pankki.fi");

    @Inject
    public SPankkiAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, SPANKKI_CONFIGURATION);
    }
}
