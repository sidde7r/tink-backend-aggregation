package se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersTransactionalAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class CrelanAgent extends Xs2aDevelopersTransactionalAgent {

    private static final String INTEGRATION_NAME = "crelan";

    public CrelanAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getIntegrationName() {
        return INTEGRATION_NAME;
    }
}
