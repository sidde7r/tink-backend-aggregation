package se.tink.backend.aggregation.agents.nxgen.uk.creditcards.amex.v62;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Agent;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.common.config.SignatureKeyPair;

public class AmericanExpressV62UKAgent extends AmericanExpressV62Agent {

    public AmericanExpressV62UKAgent(CredentialsRequest request, AgentContext context,
            SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new AmericanExpressV62UKConfiguration());
    }
}
