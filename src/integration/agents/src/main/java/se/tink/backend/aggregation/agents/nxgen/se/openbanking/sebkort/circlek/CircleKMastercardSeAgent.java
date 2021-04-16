package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebkort.circlek;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.SebBrandedCardsAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.SebBrandedCardsConstants;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CREDIT_CARDS})
public final class CircleKMastercardSeAgent extends SebBrandedCardsAgent {

    @Inject
    public CircleKMastercardSeAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, SebBrandedCardsConstants.BrandedCards.Sweden.CIRCLE_K);
    }
}
