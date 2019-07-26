package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.fasterandelskasse;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.fasterandelskasse.FasterAndelskasseConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class FasterAndelskasseAgent extends BecAgent {

    public FasterAndelskasseAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getBaseUrl() {
        return Urls.BASE_URL;
    }
}
