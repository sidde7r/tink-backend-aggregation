package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.LOANS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.MORTGAGE_AGGREGATION;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    LOANS,
    CREDIT_CARDS,
    SAVINGS_ACCOUNTS,
    IDENTITY_DATA,
    TRANSFERS,
    INVESTMENTS,
    MORTGAGE_AGGREGATION
})
@AgentPisCapability(
        capabilities = {
            PisCapability.PIS_SE_BANK_TRANSFERS,
            PisCapability.PIS_SE_BG,
            PisCapability.PIS_SE_PG,
            PisCapability.PIS_FUTURE_DATE
        },
        markets = {"SE"})
public class NordeaSEAgent extends NordeaBaseAgent {
    @Inject
    public NordeaSEAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, new NordeaSEConfiguration());
    }
}
