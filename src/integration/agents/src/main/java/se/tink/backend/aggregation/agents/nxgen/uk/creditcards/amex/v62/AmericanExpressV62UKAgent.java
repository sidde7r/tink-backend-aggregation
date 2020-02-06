package se.tink.backend.aggregation.agents.nxgen.uk.creditcards.amex.v62;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Agent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class AmericanExpressV62UKAgent extends AmericanExpressV62Agent {

    public AmericanExpressV62UKAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, new AmericanExpressV62UKConfiguration());
    }
}
