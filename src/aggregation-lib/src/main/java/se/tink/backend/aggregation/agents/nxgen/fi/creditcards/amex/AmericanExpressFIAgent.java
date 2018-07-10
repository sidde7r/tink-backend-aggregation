package se.tink.backend.aggregation.agents.nxgen.fi.creditcards.amex;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressAgent;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class AmericanExpressFIAgent extends AmericanExpressAgent {

    public AmericanExpressFIAgent(CredentialsRequest request, AgentContext context) {
        super(request, context, new AmericanExpressFIConfiguration());
    }
}
