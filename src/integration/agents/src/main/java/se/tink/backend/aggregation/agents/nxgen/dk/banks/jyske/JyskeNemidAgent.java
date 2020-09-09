package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske;

import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.JyskeNemidAuthenticator;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class JyskeNemidAgent extends JyskeAbstractAgent {

    public JyskeNemidAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected Authenticator constructAuthenticator() {

        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        JyskeNemidAuthenticator jyskeNemidAuthenticator =
                new JyskeNemidAuthenticator(
                        apiClient, client, persistentStorage, username, password, request);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        jyskeNemidAuthenticator, supplementalInformationHelper),
                jyskeNemidAuthenticator);
    }
}
