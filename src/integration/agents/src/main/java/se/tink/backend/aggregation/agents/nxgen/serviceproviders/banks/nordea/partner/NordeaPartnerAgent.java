package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner;

import com.google.inject.Inject;
import java.time.ZoneId;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import se.tink.agent.sdk.operation.Provider;
import se.tink.agent.sdk.operation.User;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.NordeaPartnerAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.NordeaPartnerJweHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.encryption.NordeaPartnerKeystore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.encryption.NordeaPartnerKeystoreProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.configuration.NordeaPartnerConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.creditcard.NordeaPartnerCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.mapper.DefaultPartnerAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.mapper.NordeaPartnerAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.NordeaPartnerTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.filter.NordeaHttpRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.filter.NordeaRefreshTimeoutFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.filter.NordeaServiceUnavailableFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.session.NordeaPartnerSessionHandler;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidasidentity.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;

@Slf4j
public abstract class NordeaPartnerAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor {

    protected NordeaPartnerAccountMapper accountMapper;

    private final NordeaPartnerApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final NordeaPartnerJweHelper jweHelper;
    private final boolean isOnStaging;

    @Inject
    public NordeaPartnerAgent(
            AgentComponentProvider componentProvider,
            NordeaPartnerKeystoreProvider keystoreProvider) {
        super(componentProvider);
        log.info("Nordea Partner: Start of the agent");
        isOnStaging =
                "neston-staging".equalsIgnoreCase(componentProvider.getContext().getClusterId())
                        || "neston-preprod"
                                .equalsIgnoreCase(componentProvider.getContext().getClusterId());
        User user = componentProvider.getUser();
        Provider provider = componentProvider.getProvider();
        apiClient =
                new NordeaPartnerApiClient(
                        client,
                        sessionStorage,
                        provider,
                        credentials,
                        getApiLocale(user.getLocale()),
                        user.isPresent());

        // Don't add signature/aggregator headers; this is not a RE agent
        client.disableAggregatorHeader();
        client.disableSignatureRequestHeader();

        transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController(
                        componentProvider.getLocalDateTimeSource());
        creditCardRefreshController =
                constructCreditCardRefreshController(componentProvider.getLocalDateTimeSource());

        NordeaPartnerConfiguration nordeaConfiguration =
                getAgentConfigurationController()
                        .getAgentConfigurationFromK8s(
                                NordeaPartnerConstants.INTEGRATION_NAME,
                                context.getClusterId(),
                                NordeaPartnerConfiguration.class);
        NordeaPartnerKeystore keystore =
                keystoreProvider.getKeystore(nordeaConfiguration, context.getClusterId());
        jweHelper = new NordeaPartnerJweHelper(keystore, nordeaConfiguration);

        apiClient.setConfiguration(nordeaConfiguration);
        apiClient.setJweHelper(jweHelper);
    }

    @Override
    protected EidasIdentity getEidasIdentity() {
        if (context.isTestContext()) {
            // use eidas proxy when running agent test
            return new EidasIdentity(
                    "oxford-preprod",
                    "c859501868b742b6bebd7a3f7911cd85",
                    "DEFAULT",
                    context.getProviderId(),
                    NordeaPartnerAgent.class);
        }
        return super.getEidasIdentity();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        if (context.isTestContext()) {
            client.setEidasProxy(configuration.getEidasProxy());
        }
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
        client.addFilter(new NordeaRefreshTimeoutFilter());
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

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController(
            LocalDateTimeSource dateTimeSource) {
        NordeaPartnerTransactionalAccountFetcher accountFetcher =
                new NordeaPartnerTransactionalAccountFetcher(
                        apiClient, getAccountMapper(), dateTimeSource, request, isOnStaging);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(accountFetcher)));
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    protected abstract ZoneId getPaginatorZoneId();

    private CreditCardRefreshController constructCreditCardRefreshController(
            LocalDateTimeSource dateTimeSource) {
        final NordeaPartnerCreditCardAccountFetcher fetcher =
                new NordeaPartnerCreditCardAccountFetcher(
                        apiClient, dateTimeSource, request, isOnStaging);
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                fetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(fetcher, 1)));
    }

    protected abstract Collection<String> getSupportedLocales();

    private String getApiLocale(String userLocale) {
        final String userLanguage = userLocale.split("_")[0];
        return getSupportedLocales().stream()
                .filter(locale -> locale.startsWith(userLanguage))
                .findFirst()
                // all agents support "en-<MARKET>" language
                .orElseGet(() -> getApiLocale("en"));
    }
}
