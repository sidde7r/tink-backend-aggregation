package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebkort.finnair;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.SebBrandedCardsAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.SebBrandedCardsConstants;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CREDIT_CARDS})
public final class FinnairMastercardSeAgent extends SebBrandedCardsAgent {

    @Inject
    public FinnairMastercardSeAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, SebBrandedCardsConstants.BrandedCards.Sweden.FINNAIR);
    }
}
