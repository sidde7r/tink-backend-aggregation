package se.tink.backend.aggregation.agents.nxgen.se.creditcards.amex.v62;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Agent;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class AmericanExpressV62SEAgent extends AmericanExpressV62Agent {

    public AmericanExpressV62SEAgent(CredentialsRequest request, AgentContext context,
            SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new AmericanExpressV62SEConfiguration());
    }
}
