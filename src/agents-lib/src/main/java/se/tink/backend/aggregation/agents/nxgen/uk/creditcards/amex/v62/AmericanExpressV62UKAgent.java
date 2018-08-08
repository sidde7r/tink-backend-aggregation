package se.tink.backend.aggregation.agents.nxgen.uk.creditcards.amex.v62;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Agent;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class AmericanExpressV62UKAgent extends AmericanExpressV62Agent {

    public AmericanExpressV62UKAgent(CredentialsRequest request, AgentContext context, String signatureKeyPath) {
        super(request, context, signatureKeyPath, new AmericanExpressV62UKConfiguration());
    }
}
