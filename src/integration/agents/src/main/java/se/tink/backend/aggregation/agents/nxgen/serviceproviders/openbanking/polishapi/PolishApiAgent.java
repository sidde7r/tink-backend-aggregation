package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Logs.LOG_TAG;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Transactions.getTransactionHistoryLengthInDays;

import com.google.inject.Inject;
import java.time.temporal.ChronoUnit;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.PolishApiAccountClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.PolishApiCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.PolishApiGetAccountClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.PolishApiPostAccountClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.PolishApiTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.PolishApiAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.PolishApiAuthorizationClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.PolishApiGetAuthorizationClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.PolishApiPostAuthorizationClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.common.filter.PolishApiPostRequestIdFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.concreteagents.PolishApiAgentCreator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishAccountsApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishAuthorizeApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishGetAccountsApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishGetAuthorizeApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishGetTransactionsApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishPostAccountsApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishPostAuthorizeApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishPostTransactionsApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory.PolishTransactionsApiUrlFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.errorhandling.PolishApiRefreshTokenFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.signature.PolishApiSignatureFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.transactions.PolishApiGetTransactionsClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.transactions.PolishApiPostTransactionsClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.transactions.PolishApiTransactionClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.transactions.PolishApiTransactionsFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.OAuth2TokenSessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.LoggingStrategy;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
public abstract class PolishApiAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                PolishApiAgentCreator {

    protected final AgentComponentProvider agentComponentProvider;
    protected final PolishApiConfiguration apiConfiguration;
    protected final PolishAccountsApiUrlFactory accountsApiUrlFactory;
    protected final PolishAuthorizeApiUrlFactory authorizeApiUrlFactory;
    protected final PolishTransactionsApiUrlFactory transactionsApiUrlFactory;

    private final PolishApiAuthorizationClient authorizationApiClient;
    private final PolishApiAccountClient accountApiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final PolishApiPersistentStorage polishPersistentStorage;

    @Inject
    public PolishApiAgent(
            AgentComponentProvider agentComponentProvider, QsealcSigner qsealcSigner) {
        super(agentComponentProvider);

        setJsonHttpTrafficLogsEnabled(true);
        client.setLoggingStrategy(LoggingStrategy.EXPERIMENTAL);

        this.agentComponentProvider = agentComponentProvider;
        this.apiConfiguration = getApiConfiguration();
        this.accountsApiUrlFactory = getAccountApiUrlFactory();
        this.authorizeApiUrlFactory = getAuthorizeApiUrlFactory();
        this.transactionsApiUrlFactory = getTransactionsApiUrlFactory();
        this.polishPersistentStorage = new PolishApiPersistentStorage(getPersistentStorage());

        ApiClientProvider apiClientProvider = new ApiClientProvider();
        this.authorizationApiClient = apiClientProvider.getAuthorizationApiClient();
        this.accountApiClient = apiClientProvider.getAccountApiClient();
        PolishApiTransactionClient transactionApiClient =
                apiClientProvider.getTransactionApiClient();
        configureHttpClient(qsealcSigner);

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController(
                        accountApiClient, transactionApiClient, agentComponentProvider);

        this.creditCardRefreshController =
                constructCreditCardRefreshController(
                        accountApiClient, transactionApiClient, agentComponentProvider);
    }

    private void configureHttpClient(QsealcSigner qsealcSigner) {
        client.addFilter(
                new PolishApiSignatureFilter(
                        qsealcSigner,
                        getConfiguration(),
                        getLogicFlowConfigurator().shouldAttachHeadersAndUriInJws()));
        client.addFilter(
                new PolishApiRefreshTokenFilter(
                        authorizationApiClient, polishPersistentStorage, 1, 10));
        if (polishPostApiAgent()) {
            client.addFilter(new PolishApiPostRequestIdFilter(agentComponentProvider));
        }
    }

    private boolean polishPostApiAgent() {
        return authorizeApiUrlFactory instanceof PolishPostAuthorizeApiUrlFactory
                && accountsApiUrlFactory instanceof PolishPostAccountsApiUrlFactory
                && transactionsApiUrlFactory instanceof PolishPostTransactionsApiUrlFactory;
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new PolishApiAuthenticator(
                                authorizationApiClient,
                                accountApiClient,
                                polishPersistentStorage,
                                getLogicFlowConfigurator()),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    protected AgentConfiguration<PolishApiConfiguration> getConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfiguration(PolishApiConfiguration.class);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController(
            PolishApiAccountClient accountsApiClient,
            PolishApiTransactionClient transactionApiClient,
            AgentComponentProvider agentComponentProvider) {
        PolishApiTransactionalAccountFetcher accountFetcher =
                new PolishApiTransactionalAccountFetcher(
                        accountsApiClient,
                        polishPersistentStorage,
                        getAccountTypeMapper(),
                        getLogicFlowConfigurator().shouldGetAccountListFromTokenResponse());
        PolishApiTransactionsFetcher<TransactionalAccount> transactionsFetcher =
                new PolishApiTransactionsFetcher(
                        transactionApiClient,
                        agentComponentProvider.getLocalDateTimeSource(),
                        agentComponentProvider.getUser(),
                        getSupportedTransactionTypes());

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(transactionsFetcher)
                                .setAmountAndUnitToFetch(
                                        getTransactionHistoryLengthInDays(getMaxDaysToFetch()),
                                        ChronoUnit.DAYS)
                                .setConsecutiveEmptyPagesLimit(0)
                                .setLocalDateTimeSource(
                                        agentComponentProvider.getLocalDateTimeSource())
                                .build()));
    }

    private CreditCardRefreshController constructCreditCardRefreshController(
            PolishApiAccountClient accountApiClient,
            PolishApiTransactionClient transactionApiClient,
            AgentComponentProvider agentComponentProvider) {
        PolishApiCreditCardAccountFetcher creditCardAccountFetcher =
                new PolishApiCreditCardAccountFetcher(
                        accountApiClient,
                        polishPersistentStorage,
                        getAccountTypeMapper(),
                        getLogicFlowConfigurator().shouldGetAccountListFromTokenResponse());

        PolishApiTransactionsFetcher<CreditCardAccount> transactionsFetcher =
                new PolishApiTransactionsFetcher<>(
                        transactionApiClient,
                        agentComponentProvider.getLocalDateTimeSource(),
                        agentComponentProvider.getUser(),
                        getSupportedTransactionTypes());

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardAccountFetcher,
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(transactionsFetcher)
                                .setAmountAndUnitToFetch(
                                        getTransactionHistoryLengthInDays(getMaxDaysToFetch()),
                                        ChronoUnit.DAYS)
                                .setConsecutiveEmptyPagesLimit(0)
                                .setLocalDateTimeSource(
                                        agentComponentProvider.getLocalDateTimeSource())
                                .build()));
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
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new OAuth2TokenSessionHandler(persistentStorage);
    }

    private class ApiClientProvider {

        private static final String POST_AND_GET_API = "Currently Api handles post and get API";

        private PolishApiAuthorizationClient getAuthorizationApiClient() {
            if (authorizeApiUrlFactory instanceof PolishPostAuthorizeApiUrlFactory) {
                return new PolishApiPostAuthorizationClient(
                        getPolishApiAgentCreator(),
                        client,
                        getConfiguration(),
                        agentComponentProvider,
                        polishPersistentStorage);
            } else if (authorizeApiUrlFactory instanceof PolishGetAuthorizeApiUrlFactory) {
                return new PolishApiGetAuthorizationClient(
                        getPolishApiAgentCreator(),
                        client,
                        getConfiguration(),
                        agentComponentProvider,
                        polishPersistentStorage);
            } else {
                throw new IllegalStateException(LOG_TAG + POST_AND_GET_API);
            }
        }

        private PolishApiAccountClient getAccountApiClient() {
            if (accountsApiUrlFactory instanceof PolishPostAccountsApiUrlFactory) {
                return new PolishApiPostAccountClient(
                        getPolishApiAgentCreator(),
                        client,
                        getConfiguration(),
                        agentComponentProvider,
                        polishPersistentStorage);
            } else if (accountsApiUrlFactory instanceof PolishGetAccountsApiUrlFactory) {
                return new PolishApiGetAccountClient(
                        getPolishApiAgentCreator(),
                        client,
                        getConfiguration(),
                        agentComponentProvider,
                        polishPersistentStorage);
            } else {
                throw new IllegalStateException(LOG_TAG + POST_AND_GET_API);
            }
        }

        private PolishApiTransactionClient getTransactionApiClient() {
            if (transactionsApiUrlFactory instanceof PolishPostTransactionsApiUrlFactory) {
                return new PolishApiPostTransactionsClient(
                        getPolishApiAgentCreator(),
                        client,
                        getConfiguration(),
                        agentComponentProvider,
                        polishPersistentStorage);
            } else if (transactionsApiUrlFactory instanceof PolishGetTransactionsApiUrlFactory) {
                return new PolishApiGetTransactionsClient(
                        getPolishApiAgentCreator(),
                        client,
                        getConfiguration(),
                        agentComponentProvider,
                        polishPersistentStorage);
            } else {
                throw new IllegalStateException(LOG_TAG + POST_AND_GET_API);
            }
        }
    }

    PolishApiAgentCreator getPolishApiAgentCreator() {
        return this;
    }
}
