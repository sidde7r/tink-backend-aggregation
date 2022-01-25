package se.tink.backend.aggregation.agents.nxgen.se.openbanking.resursbank;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration.CrosskeyMarketConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, CREDIT_CARDS})
public final class ResursBankAgent extends CrosskeyBaseAgent {

    private static final CrosskeyMarketConfiguration RESURSBANK_CONFIGURATION =
            new CrosskeyMarketConfiguration(
                    "resursbank",
                    "https://open-banking.resurs.com",
                    "https://open-banking-identification.resurs.com");

    @Inject
    public ResursBankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, RESURSBANK_CONFIGURATION);
    }
}
