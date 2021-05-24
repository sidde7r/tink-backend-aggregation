package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.authenticator.SkandiaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.configuration.SkandiaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.SkandiaPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.SkandiaTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.SkandiaTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
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
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class SkandiaAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final SkandiaApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public SkandiaAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        client.addFilter(new BankServiceInternalErrorFilter());
        apiClient = new SkandiaApiClient(client, persistentStorage, getUserIpInformation());

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    protected AgentConfiguration<SkandiaConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController().getAgentConfiguration(SkandiaConfiguration.class);
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        apiClient.setConfiguration(getAgentConfiguration(), configuration.getEidasProxy());
    }

    private SkandiaUserIpInformation getUserIpInformation() {
        return new SkandiaUserIpInformation(request.getUserAvailability().isUserPresent(), userIp);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        SkandiaPaymentExecutor skandiaPaymentExecutor = new SkandiaPaymentExecutor(apiClient);

        return Optional.of(new PaymentController(skandiaPaymentExecutor, skandiaPaymentExecutor));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController oAuth2AuthenticationController =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new SkandiaAuthenticator(
                                apiClient,
                                persistentStorage,
                                getAgentConfiguration().getProviderSpecificConfiguration()),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        oAuth2AuthenticationController, supplementalInformationHelper),
                oAuth2AuthenticationController);
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

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final SkandiaTransactionalAccountFetcher accountFetcher =
                new SkandiaTransactionalAccountFetcher(apiClient);

        final SkandiaTransactionFetcher transactionFetcher =
                new SkandiaTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(transactionFetcher)
                                .build()));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
