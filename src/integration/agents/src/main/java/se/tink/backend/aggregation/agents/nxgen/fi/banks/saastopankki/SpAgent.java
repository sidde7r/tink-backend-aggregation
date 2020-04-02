package se.tink.backend.aggregation.agents.nxgen.fi.banks.saastopankki;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkV1Configuration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SpAgent extends SamlinkAgent {

    public SpAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new SamlinkV1Configuration(SpConstants.Url.BASE));
    }
}
