package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkortv2.ingo;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CREDIT_CARDS})
public final class IngoMastercardAgent extends SebKortAgent {
    @Inject
    public IngoMastercardAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, new IngoMastercardConfiguration());
    }
}
