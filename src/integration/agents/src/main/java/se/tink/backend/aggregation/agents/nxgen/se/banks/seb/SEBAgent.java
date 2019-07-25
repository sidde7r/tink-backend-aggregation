package se.tink.backend.aggregation.agents.nxgen.se.banks.seb;

import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.SEBAuthenticator;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SEBAgent extends NextGenerationAgent {
    private final SEBApiClient apiClient;
    private final SEBSessionStorage sebSessionStorage;

    public SEBAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        final String userName = credentials.getField(Field.Key.USERNAME);
        apiClient = new SEBApiClient(client, userName);
        sebSessionStorage = new SEBSessionStorage(sessionStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(
                context, new SEBAuthenticator(apiClient, sebSessionStorage), persistentStorage);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
