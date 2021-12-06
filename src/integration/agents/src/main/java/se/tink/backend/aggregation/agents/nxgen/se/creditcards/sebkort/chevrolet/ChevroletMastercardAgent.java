package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkort.chevrolet;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.IDENTITY_DATA;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CREDIT_CARDS, IDENTITY_DATA})
public final class ChevroletMastercardAgent extends SebKortAgent {
    @Inject
    public ChevroletMastercardAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, new ChevroletMastercardConfiguration());
    }
}
