package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.nordfyns;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class NordfynsAgent extends BankdataAgent {

    public NordfynsAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(
                request,
                context,
                signatureKeyPair,
                NordfynsConstants.BASE_URL,
                NordfynsConstants.BASE_AUTH_URL);
    }
}
