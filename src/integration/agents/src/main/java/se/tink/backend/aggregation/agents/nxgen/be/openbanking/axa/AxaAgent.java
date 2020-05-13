package se.tink.backend.aggregation.agents.nxgen.be.openbanking.axa;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersTransactionalAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class AxaAgent extends Xs2aDevelopersTransactionalAgent {

    public AxaAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }
}
