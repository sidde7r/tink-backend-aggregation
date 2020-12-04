package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking;

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
import se.tink.backend.aggregation.agents.contexts.EidasContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.OpenIdAisAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.NoOpTransferDestinationAccountsProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.UkOpenBankingTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer.KeySignerProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls.TlsConfigurationSetter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.FinancialOrganisationIdFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.EidasJwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator.UkOpenBankingAuthenticationErrorMatcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator.UkOpenBankingPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator.UkOpenBankingPisAuthFilterInstantiator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServiceUnavailableBankServiceErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.instrumentation.FetcherInstrumentationRegistry;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;
import se.tink.libraries.identitydata.IdentityData;

public abstract class UkOpenBankingBaseAgent extends NextGenerationAgent
        implements RefreshTransferDestinationExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor {
    private final JwtSigner jwtSigner;

    protected UkOpenBankingApiClient apiClient;
    protected SoftwareStatementAssertion softwareStatement;
    protected ClientInfo providerConfiguration;

    private TransferDestinationRefreshController transferDestinationRefreshController;
    private CreditCardRefreshController creditCardRefreshController;
    private TransactionalAccountRefreshController transactionalAccountRefreshController;

    // Lazy loaded
    private UkOpenBankingAis aisSupport;
    private final UkOpenBankingAisConfig aisConfig;
    private final UkOpenBankingPisConfig pisConfig;
    private AccountFetcher<TransactionalAccount> transactionalAccountFetcher;

    protected final RandomValueGenerator randomValueGenerator;
    protected final LocalDateTimeSource localDateTimeSource;

    private FetcherInstrumentationRegistry fetcherInstrumentation;

    public UkOpenBankingBaseAgent(
            AgentComponentProvider componentProvider,
            JwtSigner jwtSigner,
            UkOpenBankingAisConfig aisConfig,
            UkOpenBankingPisConfig pisConfig) {
        super(componentProvider);
        this.jwtSigner = jwtSigner;
        this.aisConfig = aisConfig;
        this.pisConfig = pisConfig;
        this.randomValueGenerator = componentProvider.getRandomValueGenerator();
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
        this.fetcherInstrumentation = new FetcherInstrumentationRegistry();

        addFilter(new ServiceUnavailableBankServiceErrorFilter());
        addFilter(new FinancialOrganisationIdFilter(aisConfig.getOrganisationId()));
    }

    public UkOpenBankingBaseAgent(
            AgentComponentProvider componentProvider,
            JwtSigner jwtSigner,
            UkOpenBankingAisConfig aisConfig) {
        this(componentProvider, jwtSigner, aisConfig, null);
    }

    public void addFilter(Filter filter) {
        client.addFilter(filter);
    }

    // Different part between UkOpenBankingBaseAgent and this class
    public AgentConfiguration<? extends UkOpenBankingClientConfigurationAdapter>
            getAgentConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfiguration(getClientConfigurationFormat());
    }

    @Override
    public final void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        UkOpenBankingClientConfigurationAdapter ukOpenBankingConfiguration =
                getAgentConfiguration().getProviderSpecificConfiguration();

        softwareStatement = ukOpenBankingConfiguration.getSoftwareStatementAssertions();
        providerConfiguration = ukOpenBankingConfiguration.getProviderConfiguration();
        configureTls(ukOpenBankingConfiguration);

        final String redirectUrl = getAgentConfiguration().getRedirectUrl();

        apiClient =
                createApiClient(
                        client, jwtSigner, softwareStatement, redirectUrl, providerConfiguration);

        this.transferDestinationRefreshController = constructTransferDestinationRefreshController();

        this.creditCardRefreshController = constructCreditCardRefreshController();

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    private void configureTls(UkOpenBankingClientConfigurationAdapter ukOpenBankingConfiguration) {
        Optional<TlsConfigurationSetter> tlsConfigurationSetter =
                ukOpenBankingConfiguration.getTlsConfigurationOverride();

        /* Theres a bug in Apache http client prior to 4.5 that breaks verification chain when there
        is more than one trust store added. Therefore UK certs must be set only if we use custom
        tls config (no eidas proxy and its certificates at all).
        https://tink.slack.com/archives/CSURV2YDA/p1604673821171200
        */
        if (tlsConfigurationSetter.isPresent()) {
            client.trustRootCaCertificate(
                    UkOpenBankingV31Constants.UKOB_ROOT_CA_JKS,
                    UkOpenBankingV31Constants.UKOB_ROOT_CA_JKS_PASSWORD);

            tlsConfigurationSetter.get().applyConfiguration(client);
        } else {
            client.setEidasProxy(configuration.getEidasProxy());
        }
    }

    /** @deprecated use @link {@link KeySignerProvider} */
    @Deprecated
    protected static JwtSigner createEidasJwtSigner(
            final AgentsServiceConfiguration configuration,
            final EidasContext context,
            final Class<? extends UkOpenBankingBaseAgent> agentClass) {
        final EidasIdentity identity =
                new EidasIdentity(context.getClusterId(), context.getAppId(), agentClass);
        return new EidasJwtSigner(configuration.getEidasProxy().toInternalConfig(), identity);
    }

    protected UkOpenBankingApiClient createApiClient(
            TinkHttpClient httpClient,
            JwtSigner signer,
            SoftwareStatementAssertion softwareStatement,
            String redirectUrl,
            ClientInfo providerConfiguration) {
        return new UkOpenBankingApiClient(
                httpClient,
                signer,
                softwareStatement,
                redirectUrl,
                providerConfiguration,
                randomValueGenerator,
                persistentStorage,
                aisConfig);
    }

    @Override
    public Authenticator constructAuthenticator() {
        final OpenIdAuthenticationController openIdAuthenticationController =
                createUkOpenBankingAuthenticationController();

        return new AutoAuthenticationController(
                this.request,
                this.systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        openIdAuthenticationController, this.supplementalInformationHelper),
                openIdAuthenticationController);
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
                ais.makeCreditCardAccountFetcher(apiClient, fetcherInstrumentation),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        ais.makeCreditCardTransactionPaginatorController(apiClient)));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    protected TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController,
                new UkOpenBankingTransferDestinationFetcher(
                        new NoOpTransferDestinationAccountsProvider(),
                        AccountIdentifier.Type.SORT_CODE,
                        SortCodeIdentifier.class));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return getAisSupport()
                .makeIdentityDataFetcher(apiClient)
                .fetchIdentityData()
                .map(FetchIdentityDataResponse::new)
                .orElse(
                        new FetchIdentityDataResponse(
                                IdentityData.builder()
                                        .setFullName(null)
                                        .setDateOfBirth(null)
                                        .build()));
    }

    private AccountFetcher<TransactionalAccount> getTransactionalAccountFetcher() {
        if (Objects.nonNull(transactionalAccountFetcher)) {
            return transactionalAccountFetcher;
        }

        transactionalAccountFetcher =
                getAisSupport().makeTransactionalAccountFetcher(apiClient, fetcherInstrumentation);
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

    private OpenIdAuthenticationController createUkOpenBankingAuthenticationController() {
        return new OpenIdAuthenticationController(
                this.persistentStorage,
                this.supplementalInformationHelper,
                this.apiClient,
                new OpenIdAisAuthenticator(this.apiClient),
                this.credentials,
                this.strongAuthenticationState,
                this.request.getCallbackUri(),
                this.aisConfig.getAppToAppURL(),
                this.randomValueGenerator,
                new OpenIdAuthenticationValidator(this.apiClient));
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        if (this.pisConfig == null) {
            return Optional.empty();
        }

        UkOpenBankingPaymentApiClient paymentApiClient = createPaymentApiClient();
        OpenIdAuthenticationValidator authenticationValidator =
                new OpenIdAuthenticationValidator(paymentApiClient);
        UkOpenBankingPisAuthFilterInstantiator authFilterInstantiator =
                new UkOpenBankingPisAuthFilterInstantiator(
                        paymentApiClient, authenticationValidator);
        UkOpenBankingPaymentAuthenticator paymentAuthenticator =
                createPaymentAuthenticator(paymentApiClient, authenticationValidator);

        UkOpenBankingPaymentExecutor paymentExecutor =
                new UkOpenBankingPaymentExecutor(
                        paymentApiClient,
                        this.credentials,
                        paymentAuthenticator,
                        authFilterInstantiator);

        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }

    private UkOpenBankingPaymentAuthenticator createPaymentAuthenticator(
            UkOpenBankingPaymentApiClient paymentApiClient,
            OpenIdAuthenticationValidator authenticationValidator) {
        return new UkOpenBankingPaymentAuthenticator(
                paymentApiClient,
                authenticationValidator,
                new UkOpenBankingAuthenticationErrorMatcher(),
                this.strongAuthenticationState,
                this.supplementalInformationHelper,
                this.request.getCallbackUri(),
                this.providerConfiguration);
    }

    private UkOpenBankingPaymentApiClient createPaymentApiClient() {
        return new UkOpenBankingPaymentApiClient(
                client,
                jwtSigner,
                softwareStatement,
                getAgentConfiguration().getRedirectUrl(),
                providerConfiguration,
                randomValueGenerator,
                pisConfig);
    }
}
