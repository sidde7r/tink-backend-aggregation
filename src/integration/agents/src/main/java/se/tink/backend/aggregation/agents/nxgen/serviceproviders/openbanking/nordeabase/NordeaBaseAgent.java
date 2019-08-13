package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.configuration.NordeaBaseConfiguration;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class NordeaBaseAgent extends NextGenerationAgent {
    protected NordeaBaseApiClient apiClient;
    protected String language;

    public NordeaBaseAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        try {
            this.language = request.getUser().getLocale().split("_")[0];
        } catch (Exception e) {
            this.language = NordeaBaseConstants.QueryValues.DEFAULT_LANGUAGE;
        }
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final NordeaBaseConfiguration nordeaConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(NordeaBaseConfiguration.class);
        apiClient.setConfiguration(nordeaConfiguration);
        this.client.setEidasProxy(
                configuration.getEidasProxy(), nordeaConfiguration.getEidasQwac());
    }

    @Override
    protected abstract Authenticator constructAuthenticator();

    @Override
    protected abstract SessionHandler constructSessionHandler();
}
