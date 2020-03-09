package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.UkOpenBankingAisAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.UkOpenBankingAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.UkOpenBankingIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.UkOpenBankingTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.session.UkOpenBankingSessionHandler;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdAuthenticationFlow;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.EidasJwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public abstract class UkOpenBankingBaseAgent extends NextGenerationAgent
        implements RefreshTransferDestinationExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor {

    private final URL wellKnownURL;

    protected UkOpenBankingApiClient apiClient;
    protected SoftwareStatementAssertion softwareStatement;
    protected ProviderConfiguration providerConfiguration;
    private boolean disableSslVerification;

    private TransferDestinationRefreshController transferDestinationRefreshController;
    private CreditCardRefreshController creditCardRefreshController;
    private TransactionalAccountRefreshController transactionalAccountRefreshController;

    // Lazy loaded
    private UkOpenBankingAis aisSupport;
    private final UkOpenBankingAisConfig agentConfig;
    private UkOpenBankingAccountFetcher<?, ?, TransactionalAccount> transactionalAccountFetcher;

    private final RandomValueGenerator randomValueGenerator;

    public UkOpenBankingBaseAgent(
            AgentComponentProvider componentProvider,
            UkOpenBankingAisConfig agentConfig,
            boolean disableSslVerification) {
        super(componentProvider);
        this.wellKnownURL = agentConfig.getWellKnownURL();
        this.disableSslVerification = disableSslVerification;
        this.agentConfig = agentConfig;
        this.randomValueGenerator = componentProvider.getRandomValueGenerator();

        client.addFilter(new BankServiceInternalErrorFilter());
    }

    // Different part between UkOpenBankingBaseAgent and this class
    public UkOpenBankingClientConfigurationAdapter getClientConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfiguration(getClientConfigurationFormat());
    }

    @Override
    public final void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        UkOpenBankingClientConfigurationAdapter ukOpenBankingConfiguration =
                getClientConfiguration();

        softwareStatement = ukOpenBankingConfiguration.getSoftwareStatementAssertion();

        providerConfiguration = ukOpenBankingConfiguration.getProviderConfiguration();

        if (this.disableSslVerification) {
            client.disableSslVerification();
        } else {
            client.trustRootCaCertificate(
                    ukOpenBankingConfiguration.getRootCAData(),
                    ukOpenBankingConfiguration.getRootCAPassword());
        }

        ukOpenBankingConfiguration
                .getTlsConfigurationOverride()
                .orElse(this::useEidasProxy)
                .applyConfiguration(client);

        JwtSigner signer =
                ukOpenBankingConfiguration.getSignerOverride().orElseGet(this::getQsealSigner);

        apiClient = createApiClient(client, signer, softwareStatement, providerConfiguration);

        this.transferDestinationRefreshController = constructTransferDestinationRefreshController();

        this.creditCardRefreshController = constructCreditCardRefreshController();

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    private void useEidasProxy(TinkHttpClient httpClient) {
        httpClient.setEidasProxy(configuration.getEidasProxy());
    }

    private JwtSigner getQsealSigner() {

        return new EidasJwtSigner(
                configuration.getEidasProxy().toInternalConfig(), getEidasIdentity());
    }

    protected UkOpenBankingApiClient createApiClient(
            TinkHttpClient httpClient,
            JwtSigner signer,
            SoftwareStatementAssertion softwareStatement,
            ProviderConfiguration providerConfiguration) {
        return new UkOpenBankingApiClient(
                httpClient,
                signer,
                softwareStatement,
                providerConfiguration,
                wellKnownURL,
                randomValueGenerator,
                persistentStorage);
    }

    @Override
    protected abstract Authenticator constructAuthenticator();

    protected Authenticator constructAuthenticator(UkOpenBankingAisConfig aisConfig) {
        UkOpenBankingAisAuthenticator authenticator =
                new UkOpenBankingAisAuthenticator(apiClient, aisConfig);
        return createOpenIdFlowWithAuthenticator(authenticator, aisConfig.getAppToAppURL());
    }

    protected final Authenticator createOpenIdFlowWithAuthenticator(
            UkOpenBankingAisAuthenticator authenticator, URL appToAppRedirectURL) {
        return OpenIdAuthenticationFlow.create(
                request,
                context,
                persistentStorage,
                supplementalInformationHelper,
                authenticator,
                apiClient,
                credentials,
                strongAuthenticationState,
                appToAppRedirectURL,
                randomValueGenerator);
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
        UkOpenBankingAis ais = getAisSupport();

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                getTransactionalAccountFetcher(),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        ais.makeAccountTransactionPaginatorController(apiClient),
                        ais.makeUpcomingTransactionFetcher(apiClient).orElse(null)));
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
        UkOpenBankingAis ais = getAisSupport();

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                ais.makeCreditCardAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        ais.makeCreditCardTransactionPaginatorController(apiClient)));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    private TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new UkOpenBankingTransferDestinationFetcher());
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new UkOpenBankingSessionHandler();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        UkOpenBankingIdentityDataFetcher fetcher =
                new UkOpenBankingIdentityDataFetcher(
                        apiClient, agentConfig, IdentityDataEntity.class);
        return new FetchIdentityDataResponse(fetcher.fetchIdentityData());
    }

    private UkOpenBankingAccountFetcher<?, ?, TransactionalAccount>
            getTransactionalAccountFetcher() {
        if (Objects.nonNull(transactionalAccountFetcher)) {
            return transactionalAccountFetcher;
        }

        transactionalAccountFetcher = getAisSupport().makeTransactionalAccountFetcher(apiClient);
        return transactionalAccountFetcher;
    }

    private UkOpenBankingAis getAisSupport() {
        if (Objects.nonNull(aisSupport)) {
            return aisSupport;
        }
        aisSupport = makeAis();
        return aisSupport;
    }

    protected abstract UkOpenBankingAis makeAis();

    protected Class<? extends UkOpenBankingClientConfigurationAdapter>
            getClientConfigurationFormat() {
        return UkOpenBankingConfiguration.class;
    }
}
