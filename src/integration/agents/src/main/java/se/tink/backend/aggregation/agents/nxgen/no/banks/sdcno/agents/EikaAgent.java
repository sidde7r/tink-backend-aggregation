package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.agents;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class EikaAgent extends SdcNoAgent {
    public EikaAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }
}
