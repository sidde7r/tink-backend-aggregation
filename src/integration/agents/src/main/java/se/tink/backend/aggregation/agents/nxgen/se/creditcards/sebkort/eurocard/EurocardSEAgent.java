package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkort.eurocard;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.IDENTITY_DATA;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CREDIT_CARDS, IDENTITY_DATA})
public final class EurocardSEAgent extends SebKortAgent {
    @Inject
    public EurocardSEAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, new EurocardSEConfiguration());
    }
}
