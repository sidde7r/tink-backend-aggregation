package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkort.circlek;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CREDIT_CARDS, IDENTITY_DATA})
public final class CircleKMastercardSEAgent extends SebKortAgent {
    @Inject
    public CircleKMastercardSEAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, new CircleKMastercardSEConfiguration());
    }
}
