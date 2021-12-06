package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.mervarde;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.EnterCardConstants.BrandedCards;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CREDIT_CARDS})
public final class MervardeMasterCardAgent extends EnterCardAgent {

    @Inject
    public MervardeMasterCardAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, BrandedCards.MERVARDE);
    }
}
