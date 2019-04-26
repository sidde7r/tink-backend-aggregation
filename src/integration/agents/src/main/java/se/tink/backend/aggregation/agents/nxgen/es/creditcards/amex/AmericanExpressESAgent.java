package se.tink.backend.aggregation.agents.nxgen.es.creditcards.amex;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Agent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AmericanExpressESAgent extends AmericanExpressV62Agent {

    public AmericanExpressESAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new AmericanExpressESConfiguration());
    }

}
