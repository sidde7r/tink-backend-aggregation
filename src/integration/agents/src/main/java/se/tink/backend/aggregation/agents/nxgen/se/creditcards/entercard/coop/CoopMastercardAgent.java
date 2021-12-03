package se.tink.backend.aggregation.agents.nxgen.se.creditcards.entercard.coop;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.IDENTITY_DATA;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CREDIT_CARDS, IDENTITY_DATA})
public final class CoopMastercardAgent extends EnterCardAgent {

    @Inject
    public CoopMastercardAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, new CoopMastercardConfiguration());

        this.client.loadTrustMaterial(CoopTrustMaterialProvider.coopTrustStore(), null);
    }
}
