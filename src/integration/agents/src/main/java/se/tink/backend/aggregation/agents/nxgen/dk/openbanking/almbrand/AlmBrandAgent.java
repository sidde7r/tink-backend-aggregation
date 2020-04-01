package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.almbrand;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class AlmBrandAgent extends BankdataAgent {

    public AlmBrandAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(
                request,
                context,
                signatureKeyPair,
                AlmBrandConstants.BASE_URL,
                AlmBrandConstants.BASE_AUTH_URL);
    }
}
