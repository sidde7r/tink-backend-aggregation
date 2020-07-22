package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb;

import java.time.temporal.ChronoUnit;
import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.DkbAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.configuration.DkbConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.DkbTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.DkbPaymentExecutor;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.scaffold.ModuleDependenciesRegistry;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class DkbAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private static final int DEFAULT_MAX_CONSECUTIVE_EMPTY_PAGES = 4;
    private static final int MAX_DAYS_TO_FETCH_TRANSACTIONS = 89;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final ModuleDependenciesRegistry dependencyRegistry;

    public DkbAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        dependencyRegistry = initializeAgentDependencies(new DkbModuleDependenciesRegistration());
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        this.client.setResponseStatusHandler(new DkbResponseStatusHandler());
    }

    private AgentConfiguration<DkbConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController().getAgentConfiguration(DkbConfiguration.class);
    }

    private ModuleDependenciesRegistry initializeAgentDependencies(
            DkbModuleDependenciesRegistration moduleDependenciesRegistration) {
        final DkbConfiguration dkbConfiguration =
                getAgentConfiguration().getProviderSpecificConfiguration();
        moduleDependenciesRegistration.registerExternalDependencies(
                client,
                sessionStorage,
                persistentStorage,
                dkbConfiguration,
                supplementalInformationHelper);
        moduleDependenciesRegistration.registerInternalModuleDependencies();
        return moduleDependenciesRegistration.createModuleDependenciesRegistry();
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                dependencyRegistry.getBean(DkbAuthenticator.class));
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
        final DkbTransactionalAccountFetcher accountFetcher =
                new DkbTransactionalAccountFetcher(getApiClient());

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                accountFetcher,
                                DEFAULT_MAX_CONSECUTIVE_EMPTY_PAGES,
                                MAX_DAYS_TO_FETCH_TRANSACTIONS,
                                ChronoUnit.DAYS)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        return Optional.of(new PaymentController(new DkbPaymentExecutor(getApiClient())));
    }

    private DkbApiClient getApiClient() {
        return dependencyRegistry.getBean(DkbApiClient.class);
    }
}
