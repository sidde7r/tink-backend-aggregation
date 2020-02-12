package se.tink.backend.aggregation.agents.nxgen.be.openbanking.axa;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersTransactionalAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class AxaAgent extends Xs2aDevelopersTransactionalAgent {

    public static final String INTEGRATION_NAME = "axa";

    public AxaAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getIntegrationName() {
        return INTEGRATION_NAME;
    }
}
