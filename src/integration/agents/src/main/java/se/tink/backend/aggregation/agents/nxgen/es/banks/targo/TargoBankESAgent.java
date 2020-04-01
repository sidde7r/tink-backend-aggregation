package se.tink.backend.aggregation.agents.nxgen.es.banks.targo;

import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.es.banks.targo.session.TargoBankESSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class TargoBankESAgent extends EuroInformationAgent implements RefreshIdentityDataExecutor {
    public TargoBankESAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new TargoBankESConfiguration());
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return TargoBankESSessionHandler.create(this.apiClient, sessionStorage);
    }
}
