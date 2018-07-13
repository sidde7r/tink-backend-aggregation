package se.tink.backend.aggregation.agents.nxgen.se.creditcards.amex;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressAgent;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class AmericanExpressSEAgent extends AmericanExpressAgent {

    public AmericanExpressSEAgent(CredentialsRequest request,
            AgentContext context) {
        super(request, context, new AmericanExpressSEConfiguration());
    }
}
