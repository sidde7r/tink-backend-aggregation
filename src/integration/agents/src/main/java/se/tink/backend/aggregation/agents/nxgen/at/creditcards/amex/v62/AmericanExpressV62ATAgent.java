package se.tink.backend.aggregation.agents.nxgen.at.creditcards.amex.v62;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Agent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AmericanExpressV62ATAgent extends AmericanExpressV62Agent {

    public AmericanExpressV62ATAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new AmericanExpressV62ATConfiguration());
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {}
}
