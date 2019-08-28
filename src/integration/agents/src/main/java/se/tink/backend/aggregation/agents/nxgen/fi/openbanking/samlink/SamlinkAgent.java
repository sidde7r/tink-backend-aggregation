package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.configuration.SamlinkConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.executor.payment.SamlinkPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.fetcher.transactionalaccount.SamlinkTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupTransactionFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationFlow;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class SamlinkAgent extends BerlinGroupAgent<SamlinkApiClient, SamlinkConfiguration> {
    private final SamlinkApiClient apiClient;

    public SamlinkAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration);
        apiClient = new SamlinkApiClient(client, sessionStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return OAuth2AuthenticationFlow.create(
                request,
                systemUpdater,
                persistentStorage,
                supplementalInformationHelper,
                new SamlinkAuthenticator(apiClient),
                credentials,
                strongAuthenticationState);
    }

    @Override
    protected SamlinkApiClient getApiClient() {
        return apiClient;
    }

    @Override
    protected String getIntegrationName() {
        return SamlinkConstants.INTEGRATION_NAME;
    }

    @Override
    protected Class<SamlinkConfiguration> getConfigurationClassDescription() {
        return SamlinkConfiguration.class;
    }

    @Override
    protected BerlinGroupTransactionFetcher getTransactionFetcher() {
        return new SamlinkTransactionFetcher(getApiClient(), getConfiguration());
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        SamlinkPaymentExecutor executor = new SamlinkPaymentExecutor(getApiClient());
        return Optional.of(new PaymentController(executor, executor));
    }
}
