package se.tink.backend.aggregation.agents.nxgen.de.creditcards.amex;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Agent;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class AmericanExpressDEAgent extends AmericanExpressV62Agent {

    public AmericanExpressDEAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new AmericanExpressDEConfiguration());
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
       
    }

}
