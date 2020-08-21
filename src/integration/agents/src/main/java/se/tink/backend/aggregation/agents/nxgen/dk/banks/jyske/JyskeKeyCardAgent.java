package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.JyskeKeyCardAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.session.JyskeSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class JyskeKeyCardAgent extends BankdataAgent {

    private final JyskeApiClient apiClient;

    public JyskeKeyCardAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        this.apiClient = new JyskeApiClient(client);
        configureHttpClient(client);
    }

    @Override
    protected Authenticator constructAuthenticator() {

        JyskePersistentStorage jyskePersistentStorage =
                new JyskePersistentStorage(persistentStorage);
        JyskeKeyCardAuthenticator jyskeKeyCardAuthenticator =
                new JyskeKeyCardAuthenticator(
                        apiClient, jyskePersistentStorage, credentials, request);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new KeyCardAuthenticationController(
                        catalog, supplementalInformationHelper, jyskeKeyCardAuthenticator),
                jyskeKeyCardAuthenticator);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new JyskeSessionHandler(
                apiClient, credentials, new JyskePersistentStorage(persistentStorage));
    }
}
