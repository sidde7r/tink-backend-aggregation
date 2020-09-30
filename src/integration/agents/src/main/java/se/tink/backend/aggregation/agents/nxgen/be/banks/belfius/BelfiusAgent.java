package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAuthenticator;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.AgentPlatformStorageMigration;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.AgentPlatformStorageMigrator;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BefiusAuthenticationConfig;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence.BelfiusAgentPlatformStorageMigrator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.AuthenticatorSleepHelper;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BelfiusAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.credit.BelfiusCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.BelfiusTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.sessionhandler.BelfiusSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.signature.BelfiusSignatureCreator;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcess;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.PasswordBasedProxyConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.MultiIpGateway;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.BadGatewayRetryFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.SslHandshakeRetryFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;
import se.tink.libraries.credentials.service.CredentialsRequest;

@Slf4j
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
public final class BelfiusAgent extends NextGenerationAgent
        implements RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                AgentPlatformAuthenticator,
                AgentPlatformStorageMigration {

    private final BelfiusApiClient apiClient;
    private final AgentPlatformBelfiusApiClient agentPlatformApiClient;
    private final BelfiusSessionStorage belfiusSessionStorage;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final BelfiusSignatureCreator belfiusSignatureCreator;
    private BefiusAuthenticationConfig befiusAuthenticationConfig;
    private ObjectMapperFactory objectMapperFactory;

    public BelfiusAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());
        this.belfiusSessionStorage = new BelfiusSessionStorage(this.sessionStorage);
        configureHttpClient(agentsServiceConfiguration);
        this.belfiusSignatureCreator = new BelfiusSignatureCreator();
        this.apiClient =
                new BelfiusApiClient(
                        this.client,
                        belfiusSessionStorage,
                        getBelfiusLocale(request.getUser().getLocale()));
        this.agentPlatformApiClient =
                new AgentPlatformBelfiusApiClient(
                        new AgentPlatformHttpClient(this.client),
                        getBelfiusLocale(request.getUser().getLocale()));
        this.creditCardRefreshController = constructCreditCardRefreshController();
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
        this.objectMapperFactory = new ObjectMapperFactory();
        befiusAuthenticationConfig =
                new BefiusAuthenticationConfig(
                        agentPlatformApiClient,
                        belfiusSessionStorage,
                        belfiusSignatureCreator,
                        objectMapperFactory.getInstance());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        BelfiusAuthenticator authenticator =
                new BelfiusAuthenticator(
                        apiClient,
                        credentials,
                        persistentStorage,
                        belfiusSessionStorage,
                        supplementalInformationHelper,
                        belfiusSignatureCreator,
                        new AuthenticatorSleepHelper());

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new PasswordAuthenticationController(authenticator),
                authenticator);
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

    private void configureHttpClient(AgentsServiceConfiguration agentsServiceConfiguration) {
        client.addFilter(
                new TimeoutRetryFilter(
                        BelfiusConstants.HttpClient.MAX_RETRIES,
                        BelfiusConstants.HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(
                new BadGatewayRetryFilter(5, BelfiusConstants.HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(
                new SslHandshakeRetryFilter(
                        3, BelfiusConstants.HttpClient.RETRY_SLEEP_MILLISECONDS));

        if (agentsServiceConfiguration.isFeatureEnabled("beProxy")) {
            final PasswordBasedProxyConfiguration proxyConfiguration =
                    agentsServiceConfiguration.getCountryProxy(
                            "be", credentials.getUserId().hashCode());
            client.setProductionProxy(
                    proxyConfiguration.getHost(),
                    proxyConfiguration.getUsername(),
                    proxyConfiguration.getPassword());
            log.info(
                    "Using proxy {} with username {}",
                    proxyConfiguration.getHost(),
                    proxyConfiguration.getUsername());
        } else {
            final MultiIpGateway gateway =
                    new MultiIpGateway(client, credentials.getUserId(), credentials.getId());
            gateway.setMultiIpGateway(agentsServiceConfiguration.getIntegrations());
        }
        client.disableAggregatorHeader();
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        BelfiusTransactionalAccountFetcher transactionalAccountFetcher =
                new BelfiusTransactionalAccountFetcher(this.apiClient, this.belfiusSessionStorage);
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                transactionalAccountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        transactionalAccountFetcher,
                        transactionalAccountFetcher));
    }

    private String getBelfiusLocale(String userLocale) {
        if (Strings.isNullOrEmpty(userLocale)) {
            return BelfiusConstants.Request.LOCALE_DUTCH;
        }
        if (userLocale.toLowerCase().contains(BelfiusConstants.TINK_FRENCH)) {
            return BelfiusConstants.Request.LOCALE_FRENCH;
        }
        return BelfiusConstants.Request.LOCALE_DUTCH;
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        BelfiusCreditCardFetcher accountFetcher = new BelfiusCreditCardFetcher(this.apiClient);
        return new CreditCardRefreshController(
                this.metricRefreshController,
                this.updateController,
                accountFetcher,
                accountFetcher);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new BelfiusSessionHandler(this.apiClient);
    }

    @Override
    public AgentAuthenticationProcess getAuthenticationProcess() {
        return befiusAuthenticationConfig.belfiusAuthProcess();
    }

    @Override
    public AgentPlatformStorageMigrator getMigrator() {
        return new BelfiusAgentPlatformStorageMigrator(
                credentials, objectMapperFactory.getInstance());
    }
}
