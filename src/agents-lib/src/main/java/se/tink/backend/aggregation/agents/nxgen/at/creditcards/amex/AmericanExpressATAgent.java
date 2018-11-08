package se.tink.backend.aggregation.agents.nxgen.at.creditcards.amex;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressAgent;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class AmericanExpressATAgent extends AmericanExpressAgent {

    public AmericanExpressATAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new AmericanExpressATConfiguration());
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
       
    }

}
