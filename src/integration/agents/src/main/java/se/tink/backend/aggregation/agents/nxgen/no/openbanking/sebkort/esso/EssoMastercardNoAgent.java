package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sebkort.esso;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.SebBrandedCardsAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.SebBrandedCardsConstants;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CREDIT_CARDS})
public final class EssoMastercardNoAgent extends SebBrandedCardsAgent {

    @Inject
    public EssoMastercardNoAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, SebBrandedCardsConstants.BrandedCards.Norway.ESSO);
    }
}
