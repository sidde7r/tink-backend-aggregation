package se.tink.backend.aggregation.agents.nxgen.de.creditcards.amex;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Agent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CREDIT_CARDS})
public final class AmericanExpressDEAgent extends AmericanExpressV62Agent {

    @Inject
    public AmericanExpressDEAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, new AmericanExpressDEConfiguration());
    }
}
