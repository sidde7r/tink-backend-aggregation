package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.NordeaPartnerAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.NordeaPartnerJweHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.encryption.NordeaPartnerKeystore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.configuration.NordeaPartnerConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.session.NordeaPartnerSessionHandler;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NordeaPartnerAgent extends NextGenerationAgent {

    private final String clientName;
    private final NordeaPartnerApiClient apiClient;
    private NordeaPartnerKeystore keystore;
    private NordeaPartnerConfiguration nordeaConfiguration;

    protected NordeaPartnerAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new NordeaPartnerApiClient(client, sessionStorage);
        clientName = request.getProvider().getPayload();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        nordeaConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                NordeaPartnerConstants.INTEGRATION_NAME,
                                clientName,
                                NordeaPartnerConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                String.format(
                                                        "No Nordea Partner client configured for name: %s",
                                                        clientName)));
        apiClient.setConfiguration(nordeaConfiguration);
        keystore = new NordeaPartnerKeystore(nordeaConfiguration);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        NordeaPartnerJweHelper jweHelper =
                new NordeaPartnerJweHelper(keystore, nordeaConfiguration);
        NordeaPartnerAuthenticator authenticator =
                new NordeaPartnerAuthenticator(
                        supplementalInformationController,
                        persistentStorage,
                        sessionStorage,
                        jweHelper);
        return new AutoAuthenticationController(
                request, systemUpdater, authenticator, authenticator);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new NordeaPartnerSessionHandler(sessionStorage);
    }
}
