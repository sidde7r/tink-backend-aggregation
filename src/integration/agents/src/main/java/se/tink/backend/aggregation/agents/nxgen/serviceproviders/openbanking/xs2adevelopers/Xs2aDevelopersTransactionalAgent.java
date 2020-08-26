package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers;

import java.security.cert.CertificateException;
import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.Xs2aDevelopersPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.Xs2aDevelopersPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.Xs2aDevelopersTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public abstract class Xs2aDevelopersTransactionalAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    protected final Xs2aDevelopersProviderConfiguration configuration;

    protected final TransactionalAccountRefreshController transactionalAccountRefreshController;
    protected Xs2aDevelopersApiClient apiClient;
    protected Xs2aDevelopersAuthenticator oauth2Authenticator;

    protected Xs2aDevelopersTransactionalAgent(
            AgentComponentProvider componentProvider, String baseUrl) {
        super(componentProvider);

        configuration = getConfiguration(baseUrl);
        apiClient = new Xs2aDevelopersApiClient(client, persistentStorage, configuration);
        oauth2Authenticator =
                new Xs2aDevelopersAuthenticator(apiClient, persistentStorage, configuration);
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        this.client.setEidasProxy(configuration.getEidasProxy());
    }

    private Xs2aDevelopersProviderConfiguration getConfiguration(String baseUrl) {
        AgentConfiguration<Xs2aDevelopersConfiguration> agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(Xs2aDevelopersConfiguration.class);
        String organizationIdentifier;
        try {
            organizationIdentifier =
                    CertificateUtils.getOrganizationIdentifier(agentConfiguration.getQwac());
        } catch (CertificateException e) {
            throw new IllegalStateException("Could not extract organization identifier!", e);
        }
        String redirectUrl = agentConfiguration.getRedirectUrl();
        return new Xs2aDevelopersProviderConfiguration(
                organizationIdentifier, baseUrl, redirectUrl);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        oauth2Authenticator,
                        credentials,
                        strongAuthenticationState,
                        request);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }

    protected TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final Xs2aDevelopersTransactionalAccountFetcher accountFetcher =
                new Xs2aDevelopersTransactionalAccountFetcher(apiClient, oauth2Authenticator);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(accountFetcher)));
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new Xs2aDevelopersPaymentAuthenticator(
                                apiClient, persistentStorage, configuration),
                        credentials,
                        strongAuthenticationState,
                        request);

        Xs2aDevelopersPaymentExecutor xs2aDevelopersPaymentExecutor =
                new Xs2aDevelopersPaymentExecutor(
                        apiClient,
                        new ThirdPartyAppAuthenticationController<>(
                                controller, supplementalInformationHelper),
                        credentials,
                        persistentStorage);

        return Optional.of(
                new PaymentController(
                        xs2aDevelopersPaymentExecutor, xs2aDevelopersPaymentExecutor));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
