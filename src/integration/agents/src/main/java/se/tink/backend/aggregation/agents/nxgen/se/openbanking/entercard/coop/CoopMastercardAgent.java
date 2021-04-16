package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.coop;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardConstants.BrandedCards;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CREDIT_CARDS})
public final class CoopMastercardAgent extends EnterCardAgent {

    @Inject
    public CoopMastercardAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, BrandedCards.COOP);
    }
}
