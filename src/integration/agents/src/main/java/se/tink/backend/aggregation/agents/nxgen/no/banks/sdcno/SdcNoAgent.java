package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.authenticator.SdcNoBankIdSSAuthenticator;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SdcNoAgent extends NextGenerationAgent {
    private SdcNoConfiguration configuration;

    public SdcNoAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        this.configuration = new SdcNoConfiguration(request.getProvider());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new SdcNoBankIdSSAuthenticator(configuration);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
