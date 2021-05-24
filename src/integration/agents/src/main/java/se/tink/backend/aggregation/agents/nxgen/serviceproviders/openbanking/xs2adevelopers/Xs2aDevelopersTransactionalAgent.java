package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers;

import java.security.cert.CertificateException;
import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticatorHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersDecoupledAuthenticatior;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersRedirectAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.Xs2aDevelopersPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.Xs2aDevelopersPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.Xs2aDevelopersTransactionDateFromFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.Xs2aDevelopersTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.filters.TransactionFetchRetryFilter;
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
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionKeyWithInitDateFromFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceDownExceptionFilter;

public abstract class Xs2aDevelopersTransactionalAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    protected final Xs2aDevelopersProviderConfiguration configuration;
    protected final Xs2aDevelopersApiClient apiClient;
    protected final Xs2aDevelopersAuthenticatorHelper authenticatorHelper;
    protected final TransactionalAccountRefreshController transactionalAccountRefreshController;

    protected Xs2aDevelopersTransactionalAgent(
            AgentComponentProvider componentProvider, String baseUrl) {
        super(componentProvider);
        configuration = getConfiguration(baseUrl);
        apiClient = constructApiClient(componentProvider);
        authenticatorHelper = constructXs2aAuthenticator(componentProvider);
        transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController(componentProvider);
    }

    protected Xs2aDevelopersProviderConfiguration getConfiguration(String baseUrl) {
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

    protected Xs2aDevelopersApiClient constructApiClient(AgentComponentProvider componentProvider) {
        return new Xs2aDevelopersApiClient(
                componentProvider.getTinkHttpClient(),
                persistentStorage,
                configuration,
                request.isManual(),
                userIp,
                componentProvider.getRandomValueGenerator());
    }

    protected Xs2aDevelopersAuthenticatorHelper constructXs2aAuthenticator(
            AgentComponentProvider componentProvider) {
        return new Xs2aDevelopersAuthenticatorHelper(
                apiClient,
                persistentStorage,
                configuration,
                componentProvider.getLocalDateTimeSource(),
                credentials);
    }

    protected TransactionalAccountRefreshController constructTransactionalAccountRefreshController(
            AgentComponentProvider agentComponentProvider) {
        final Xs2aDevelopersTransactionalAccountFetcher accountFetcher =
                new Xs2aDevelopersTransactionalAccountFetcher(apiClient, authenticatorHelper);

        final TransactionFetcher<TransactionalAccount> transactionFetcher =
                new TransactionKeyWithInitDateFromFetcherController<>(
                        request,
                        new Xs2aDevelopersTransactionDateFromFetcher<TransactionalAccount>(
                                apiClient,
                                agentComponentProvider.getLocalDateTimeSource(),
                                request.getUserAvailability().isUserPresent()));

        return new TransactionalAccountRefreshController(
                metricRefreshController, updateController, accountFetcher, transactionFetcher);
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        this.client.setEidasProxy(configuration.getEidasProxy());
        this.client.addFilter(new BankServiceDownExceptionFilter());
        this.client.addFilter(new TransactionFetchRetryFilter());
    }

    private Xs2aDevelopersRedirectAuthenticator constructRedirectAuthenticator() {
        final OAuth2AuthenticationController oAuth2Controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        authenticatorHelper,
                        credentials,
                        strongAuthenticationState);
        return new Xs2aDevelopersRedirectAuthenticator(
                oAuth2Controller, supplementalInformationHelper, authenticatorHelper);
    }

    private Xs2aDevelopersDecoupledAuthenticatior constructDecoupledAuthenticator() {
        return new Xs2aDevelopersDecoupledAuthenticatior(
                authenticatorHelper,
                supplementalInformationController,
                supplementalInformationFormer);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        Xs2aDevelopersAuthenticator authenticator =
                new Xs2aDevelopersAuthenticator(
                        constructRedirectAuthenticator(),
                        constructDecoupledAuthenticator(),
                        this.authenticatorHelper);
        return new AutoAuthenticationController(
                request, systemUpdater, authenticator, authenticator);
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

    @Override
    public Optional<PaymentController> constructPaymentController() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new Xs2aDevelopersPaymentAuthenticator(
                                apiClient, persistentStorage, configuration),
                        credentials,
                        strongAuthenticationState);

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
