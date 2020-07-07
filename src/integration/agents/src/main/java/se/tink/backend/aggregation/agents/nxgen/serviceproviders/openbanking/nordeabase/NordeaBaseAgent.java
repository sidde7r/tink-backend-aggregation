package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.configuration.NordeaBaseConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
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
        } catch (RuntimeException e) {
            this.language = NordeaBaseConstants.QueryValues.DEFAULT_LANGUAGE;
        }
    }

    public NordeaBaseAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        try {
            this.language = request.getUser().getLocale().split("_")[0];
        } catch (RuntimeException e) {
            this.language = NordeaBaseConstants.QueryValues.DEFAULT_LANGUAGE;
        }
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        apiClient.setConfiguration(
                getAgentConfiguration(), configuration.getEidasProxy(), getEidasIdentity());
        this.client.setEidasProxy(configuration.getEidasProxy());
    }

    protected AgentConfiguration<NordeaBaseConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfiguration(NordeaBaseConfiguration.class);
    }

    @Override
    protected abstract Authenticator constructAuthenticator();

    @Override
    protected abstract SessionHandler constructSessionHandler();
}
