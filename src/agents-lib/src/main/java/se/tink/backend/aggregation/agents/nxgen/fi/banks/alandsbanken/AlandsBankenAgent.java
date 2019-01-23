package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class AlandsBankenAgent extends CrossKeyAgent {

    public AlandsBankenAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new AlandsBankenFIConfiguration());
    }
}
