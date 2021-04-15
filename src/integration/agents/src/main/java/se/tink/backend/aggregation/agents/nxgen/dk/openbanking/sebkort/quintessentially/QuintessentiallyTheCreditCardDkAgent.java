package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sebkort.quintessentially;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.SebBrandedCardsAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.SebBrandedCardsConstants;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CREDIT_CARDS})
public final class QuintessentiallyTheCreditCardDkAgent extends SebBrandedCardsAgent {

    @Inject
    public QuintessentiallyTheCreditCardDkAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, SebBrandedCardsConstants.BrandedCards.Denmark.QUINTESSENTIALLY);
    }
}
