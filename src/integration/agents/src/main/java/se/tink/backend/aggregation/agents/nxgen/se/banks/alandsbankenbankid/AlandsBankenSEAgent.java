package se.tink.backend.aggregation.agents.nxgen.se.banks.alandsbankenbankid;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AlandsBankenSEAgent extends CrossKeyAgent {

    public AlandsBankenSEAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new AlandsBankenSEConfiguration());
    }
}
