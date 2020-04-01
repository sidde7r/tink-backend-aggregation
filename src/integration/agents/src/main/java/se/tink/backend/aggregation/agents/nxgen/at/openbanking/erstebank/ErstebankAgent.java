package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.ErstebankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.configuration.ErstebankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.executor.payment.ErstebankPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.fetcher.transactionalaccount.ErstebankTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupTransactionFetcher;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationFlow;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class ErstebankAgent
        extends BerlinGroupAgent<ErstebankApiClient, ErstebankConfiguration> {
    private final ErstebankApiClient apiClient;

    public ErstebankAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration);

        apiClient = new ErstebankApiClient(client, sessionStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return OAuth2AuthenticationFlow.create(
                request,
                systemUpdater,
                persistentStorage,
                supplementalInformationHelper,
                new ErstebankAuthenticator(apiClient, sessionStorage),
                credentials,
                strongAuthenticationState);
    }

    @Override
    protected ErstebankApiClient getApiClient() {
        return apiClient;
    }

    @Override
    protected Class<ErstebankConfiguration> getConfigurationClassDescription() {
        return ErstebankConfiguration.class;
    }

    @Override
    protected BerlinGroupTransactionFetcher getTransactionFetcher() {
        return new ErstebankTransactionFetcher(apiClient);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        ErstebankPaymentExecutor executor = new ErstebankPaymentExecutor(apiClient);
        return Optional.of(new PaymentController(executor, executor));
    }
}
