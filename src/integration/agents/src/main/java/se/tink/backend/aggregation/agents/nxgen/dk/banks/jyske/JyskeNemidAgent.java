package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.JyskeNemidAuthenticator;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppAuthenticationController;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, INVESTMENTS, LOANS})
public final class JyskeNemidAgent extends JyskeAbstractAgent {

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
                        apiClient, client, persistentStorage, username, password);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new NemIdCodeAppAuthenticationController(
                        jyskeNemidAuthenticator,
                        credentials,
                        supplementalInformationController,
                        catalog),
                jyskeNemidAuthenticator);
    }
}
