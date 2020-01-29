package se.tink.backend.aggregation.agents.nxgen.uk.creditcards.amex.v62;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Agent;
import se.tink.backend.aggregation.nxgen.agents.strategy.SubsequentGenerationAgentStrategy;

public class AmericanExpressV62UKAgent extends AmericanExpressV62Agent {

    public AmericanExpressV62UKAgent(SubsequentGenerationAgentStrategy agentStrategy) {
        super(agentStrategy, new AmericanExpressV62UKConfiguration());
    }
}
