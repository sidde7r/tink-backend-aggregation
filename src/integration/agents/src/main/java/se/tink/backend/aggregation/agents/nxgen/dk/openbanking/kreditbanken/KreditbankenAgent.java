package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.kreditbanken;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class KreditbankenAgent extends BankdataAgent {

    public KreditbankenAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(
                request,
                context,
                signatureKeyPair,
                KreditbankenConstants.BASE_URL,
                KreditbankenConstants.BASE_AUTH_URL);
    }
}
