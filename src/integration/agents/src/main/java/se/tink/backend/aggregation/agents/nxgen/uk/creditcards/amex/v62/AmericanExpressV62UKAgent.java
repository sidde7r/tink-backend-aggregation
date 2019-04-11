package se.tink.backend.aggregation.agents.nxgen.uk.creditcards.amex.v62;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Agent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AmericanExpressV62UKAgent extends AmericanExpressV62Agent {

    public AmericanExpressV62UKAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new AmericanExpressV62UKConfiguration());
    }
}
