package se.tink.backend.aggregation.agents.nxgen.be.creditcards.amex;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressAgent;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.common.config.SignatureKeyPair;

public class AmericanExpressBEAgent extends AmericanExpressAgent {

    public AmericanExpressBEAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new AmericanExpressBEConfiguration());
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
       
    }

}
