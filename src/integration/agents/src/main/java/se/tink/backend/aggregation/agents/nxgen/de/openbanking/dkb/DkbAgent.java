package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
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
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.scaffold.ModuleDependenciesRegistry;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.i18n.Catalog;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class DkbAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

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

        Map<Class<?>, Object> beans = new HashMap<>();
        beans.put(DkbConfiguration.class, dkbConfiguration);
        beans.put(DkbUserIpInformation.class, new DkbUserIpInformation(request.isManual(), userIp));
        beans.put(Catalog.class, catalog);
        beans.put(Credentials.class, credentials);
        beans.put(SupplementalInformationHelper.class, supplementalInformationHelper);

        moduleDependenciesRegistration.registerExternalDependencies(
                client, sessionStorage, persistentStorage, beans);
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
        DkbAuthenticator authenticator = dependencyRegistry.getBean(DkbAuthenticator.class);
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

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final DkbTransactionalAccountFetcher accountFetcher =
                new DkbTransactionalAccountFetcher(getApiClient());

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(accountFetcher)
                                .setAmountAndUnitToFetch(88, ChronoUnit.DAYS)
                                .build()));
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
