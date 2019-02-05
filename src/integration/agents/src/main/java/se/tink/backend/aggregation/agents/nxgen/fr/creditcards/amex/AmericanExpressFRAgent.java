package se.tink.backend.aggregation.agents.nxgen.fr.creditcards.amex;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressAgent;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class AmericanExpressFRAgent extends AmericanExpressAgent {

    public AmericanExpressFRAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new AmericanExpressFRConfiguration());
    }
}
