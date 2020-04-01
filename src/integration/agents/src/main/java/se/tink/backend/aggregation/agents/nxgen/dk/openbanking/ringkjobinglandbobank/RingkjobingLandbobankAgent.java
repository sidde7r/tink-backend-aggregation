package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.ringkjobinglandbobank;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class RingkjobingLandbobankAgent extends BankdataAgent {

    public RingkjobingLandbobankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(
                request,
                context,
                signatureKeyPair,
                RingkjobingLandbobankConstants.BASE_URL,
                RingkjobingLandbobankConstants.BASE_AUTH_URL);
    }
}
