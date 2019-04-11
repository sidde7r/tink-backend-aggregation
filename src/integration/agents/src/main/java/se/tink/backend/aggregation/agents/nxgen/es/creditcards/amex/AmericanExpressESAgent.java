package se.tink.backend.aggregation.agents.nxgen.es.creditcards.amex;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AmericanExpressESAgent extends AmericanExpressAgent {

    public AmericanExpressESAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new AmericanExpressESConfiguration());
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {}
}
