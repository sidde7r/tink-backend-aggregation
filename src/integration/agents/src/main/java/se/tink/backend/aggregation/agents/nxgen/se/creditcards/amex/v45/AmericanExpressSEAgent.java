package se.tink.backend.aggregation.agents.nxgen.se.creditcards.amex.v45;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressAgent;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class AmericanExpressSEAgent extends AmericanExpressAgent {

    public AmericanExpressSEAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new AmericanExpressSEConfiguration());
    }
}
