package se.tink.backend.aggregation.agents.nxgen.at.creditcards.amex.v62;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.IDENTITY_DATA;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Agent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CREDIT_CARDS, IDENTITY_DATA})
public final class AmericanExpressV62ATAgent extends AmericanExpressV62Agent {

    @Inject
    public AmericanExpressV62ATAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, new AmericanExpressV62ATConfiguration());
    }
}
