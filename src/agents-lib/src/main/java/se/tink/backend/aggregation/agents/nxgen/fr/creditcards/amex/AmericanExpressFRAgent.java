package se.tink.backend.aggregation.agents.nxgen.fr.creditcards.amex;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressAgent;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class AmericanExpressFRAgent extends AmericanExpressAgent {

    public AmericanExpressFRAgent(CredentialsRequest request, AgentContext context) {
        super(request, context, new AmericanExpressFRConfiguration());
    }
}
