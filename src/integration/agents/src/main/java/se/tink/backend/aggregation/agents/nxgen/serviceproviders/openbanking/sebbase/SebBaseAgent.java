package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase;

import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.SebDecoupledAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.configuration.SebConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.session.SebSessionHandler;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class SebBaseAgent<C extends SebBaseApiClient> extends NextGenerationAgent
        implements RefreshCreditCardAccountsExecutor {

    protected C apiClient;
    protected CreditCardRefreshController creditCardRefreshController;
    protected AgentConfiguration<SebConfiguration> agentConfiguration;
    protected SebConfiguration sebConfiguration;

    protected SebBaseAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.apiClient = getApiClient();
    }

    protected SebBaseAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.apiClient = getApiClient();
    }

    protected abstract C getApiClient();

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        agentConfiguration =
                getAgentConfigurationController().getAgentConfiguration(SebConfiguration.class);
        sebConfiguration = agentConfiguration.getProviderSpecificConfiguration();
        apiClient.setConfiguration(sebConfiguration);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        BankIdAuthenticationController bankIdAuthenticationController =
                new BankIdAuthenticationController<>(
                        supplementalInformationController,
                        new SebDecoupledAuthenticator(
                                apiClient, agentConfiguration, request.getUser().getLocale()),
                        persistentStorage,
                        credentials);
        return new AutoAuthenticationController(
                request,
                systemUpdater,
                bankIdAuthenticationController,
                bankIdAuthenticationController);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SebSessionHandler(apiClient, persistentStorage);
    }
}
