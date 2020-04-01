package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.NordeaPartnerAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.NordeaPartnerJweHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.encryption.NordeaPartnerKeystore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.configuration.NordeaPartnerConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.mapper.DefaultPartnerAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.mapper.NordeaPartnerAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.NordeaPartnerTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.filter.NordeaHttpRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.filter.NordeaServiceUnavailableFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.session.NordeaPartnerSessionHandler;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class NordeaPartnerAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final NordeaPartnerApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private NordeaPartnerJweHelper jweHelper;
    protected NordeaPartnerAccountMapper accountMapper;

    public NordeaPartnerAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());
        apiClient = new NordeaPartnerApiClient(client, sessionStorage, credentials);
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();

        configureHttpClient(agentsServiceConfiguration);

        NordeaPartnerConfiguration nordeaConfiguration =
                getAgentConfigurationController()
                        .getAgentConfigurationFromK8s(
                                NordeaPartnerConstants.INTEGRATION_NAME,
                                context.getClusterId(),
                                NordeaPartnerConfiguration.class);
        NordeaPartnerKeystore keystore =
                new NordeaPartnerKeystore(nordeaConfiguration, context.getClusterId());
        jweHelper = new NordeaPartnerJweHelper(keystore, nordeaConfiguration);

        apiClient.setConfiguration(nordeaConfiguration);
        apiClient.setJweHelper(jweHelper);
    }

    private void configureHttpClient(AgentsServiceConfiguration configuration) {
        client.setEidasProxy(configuration.getEidasProxy());
        client.addFilter(new BankServiceInternalErrorFilter());
        client.addFilter(
                new TimeoutRetryFilter(
                        NordeaPartnerConstants.HttpFilters.MAX_NUM_RETRIES,
                        NordeaPartnerConstants.HttpFilters.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(
                new NordeaHttpRetryFilter(
                        NordeaPartnerConstants.HttpFilters.MAX_NUM_RETRIES,
                        NordeaPartnerConstants.HttpFilters.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(new NordeaServiceUnavailableFilter());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        NordeaPartnerAuthenticator authenticator =
                new NordeaPartnerAuthenticator(credentials, sessionStorage, jweHelper);
        return new AutoAuthenticationController(
                request, systemUpdater, authenticator, authenticator);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new NordeaPartnerSessionHandler(sessionStorage);
    }

    protected NordeaPartnerAccountMapper getAccountMapper() {
        if (accountMapper == null) {
            accountMapper = new DefaultPartnerAccountMapper();
        }
        return accountMapper;
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

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        NordeaPartnerTransactionalAccountFetcher accountFetcher =
                new NordeaPartnerTransactionalAccountFetcher(apiClient, getAccountMapper());

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(accountFetcher)));
    }
}
