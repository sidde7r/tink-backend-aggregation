package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshBeneficiariesExecutor;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.TypedPaymentControllerable;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.OpenIdAisAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.NoOpTransferDestinationAccountsProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.UkOpenBankingTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls.LocalCertificateTlsConfigurationSetter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls.TlsConfigurationSetter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.PartyMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.ConsentErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.FinancialOrganisationIdFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator.UkOpenBankingAuthenticationErrorMatcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator.UkOpenBankingPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator.UkOpenBankingPisAuthApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator.UkOpenBankingPisAuthFilterInstantiator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.UkOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.UkOpenBankingPaymentHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.UkOpenBankingRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.filter.UkOpenBankingPisRequestFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.UkOpenBankingJwtSignatureHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.UkOpenBankingPs256Base64SignatureCreator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.UkOpenBankingPs256MinimalSignatureCreator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.UkOpenBankingPs256SignatureCreator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.UkOpenBankingPs256WithoutBase64SignatureCreator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.UkOpenBankingRs256SignatureCreator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.credentialsupdater.UkOpenBankingCredentialsUpdater;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.DomesticPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.converter.DomesticPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.DomesticScheduledPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.converter.DomesticScheduledPaymentConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.storage.UkOpenBankingPaymentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.validator.DefaultUkOpenBankingPaymentRequestValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.validator.UkOpenBankingPaymentRequestValidator;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
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
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;
import se.tink.libraries.concurrency.RunnableMdcWrapper;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public abstract class UkOpenBankingBaseAgent extends NextGenerationAgent
        implements RefreshBeneficiariesExecutor,
                RefreshTransferDestinationExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor,
                TypedPaymentControllerable {
    private final JwtSigner jwtSigner;
    private TlsConfigurationSetter tlsConfigurationSetter;
    private AgentConfiguration<? extends UkOpenBankingClientConfigurationAdapter>
            agentConfiguration;

    private final UkOpenBankingAisConfig aisConfig;
    private final UkOpenBankingPisConfig pisConfig;

    private final FetcherInstrumentationRegistry fetcherInstrumentation;
    private final UkOpenBankingPisRequestFilter pisRequestFilter;

    protected final RandomValueGenerator randomValueGenerator;
    protected final LocalDateTimeSource localDateTimeSource;
    // Lazy loaded
    private UkOpenBankingAis aisSupport;
    private AccountFetcher<TransactionalAccount> transactionalAccountFetcher;
    private TransferDestinationRefreshController transferDestinationRefreshController;
    private CreditCardRefreshController creditCardRefreshController;
    private TransactionalAccountRefreshController transactionalAccountRefreshController;

    protected UkOpenBankingApiClient apiClient;
    protected SoftwareStatementAssertion softwareStatement;
    protected ClientInfo providerConfiguration;
    private String redirectUrl;

    public UkOpenBankingBaseAgent(
            AgentComponentProvider componentProvider,
            UkOpenBankingFlowFacade ukOpenBankingFlowFacade,
            UkOpenBankingAisConfig aisConfig,
            UkOpenBankingPisConfig pisConfig,
            UkOpenBankingPisRequestFilter pisRequestFilter) {
        super(componentProvider);
        this.jwtSigner = ukOpenBankingFlowFacade.getJwtSinger();
        this.agentConfiguration = ukOpenBankingFlowFacade.getAgentConfiguration();
        this.tlsConfigurationSetter = ukOpenBankingFlowFacade.getTlsConfigurationSetter();
        this.aisConfig = aisConfig;
        this.pisConfig = pisConfig;
        this.randomValueGenerator = componentProvider.getRandomValueGenerator();
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
        this.fetcherInstrumentation = new FetcherInstrumentationRegistry();
        this.pisRequestFilter = pisRequestFilter;
        configureMdcPropagation();
    }

    public UkOpenBankingBaseAgent(
            AgentComponentProvider componentProvider,
            UkOpenBankingFlowFacade ukOpenBankingFlowFacade,
            UkOpenBankingAisConfig aisConfig) {
        this(componentProvider, ukOpenBankingFlowFacade, aisConfig, null, null);
    }

    /** @deprecated please use new construction with {@link UkOpenBankingFlowFacade} */
    @Deprecated
    public UkOpenBankingBaseAgent(
            AgentComponentProvider componentProvider,
            JwtSigner jwtSigner,
            UkOpenBankingAisConfig aisConfig,
            UkOpenBankingPisConfig pisConfig,
            UkOpenBankingPisRequestFilter pisRequestFilter) {
        super(componentProvider);
        this.jwtSigner = jwtSigner;
        this.aisConfig = aisConfig;
        this.pisConfig = pisConfig;
        this.randomValueGenerator = componentProvider.getRandomValueGenerator();
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
        this.fetcherInstrumentation = new FetcherInstrumentationRegistry();
        this.pisRequestFilter = pisRequestFilter;
        configureMdcPropagation();
    }

    /** @deprecated please new construction with {@link UkOpenBankingFlowFacade} */
    @Deprecated
    public UkOpenBankingBaseAgent(
            AgentComponentProvider componentProvider,
            JwtSigner jwtSigner,
            UkOpenBankingAisConfig aisConfig) {
        this(componentProvider, jwtSigner, aisConfig, null, null);
    }

    private void addFilter(Filter filter) {
        client.addFilter(filter);
    }

    /** @deprecated First step in the refactor, it's gonna be provided from IoC */
    // Different part between UkOpenBankingBaseAgent and this class
    @Deprecated
    protected AgentConfiguration<? extends UkOpenBankingClientConfigurationAdapter>
            getAgentConfiguration() {
        if (agentConfiguration != null) {
            return agentConfiguration;
        } else {
            return getAgentConfigurationController()
                    .getAgentConfiguration(getClientConfigurationFormat());
        }
    }

    /** @deprecated First step in the refactor, it's gonna be provided from IoC */
    @Deprecated
    protected Class<? extends UkOpenBankingClientConfigurationAdapter>
            getClientConfigurationFormat() {
        return UkOpenBankingConfiguration.class;
    }

    @Override
    public final void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        UkOpenBankingClientConfigurationAdapter ukOpenBankingConfiguration =
                getAgentConfiguration().getProviderSpecificConfiguration();

        softwareStatement = ukOpenBankingConfiguration.getSoftwareStatementAssertions();
        providerConfiguration = ukOpenBankingConfiguration.getProviderConfiguration();
        redirectUrl = getAgentConfiguration().getRedirectUrl();
        configureTls(ukOpenBankingConfiguration);
        setSoftwareStatementForSignatureCreator(softwareStatement);

        apiClient =
                createApiClient(
                        client, jwtSigner, softwareStatement, redirectUrl, providerConfiguration);

        this.transferDestinationRefreshController = constructTransferDestinationRefreshController();

        this.creditCardRefreshController = constructCreditCardRefreshController();

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();

        addFilter(new ServiceUnavailableBankServiceErrorFilter());
        addFilter(new FinancialOrganisationIdFilter(aisConfig.getOrganisationId()));
        addFilter(new ConsentErrorFilter(persistentStorage));
    }

    /**
     * @deprecated First step in the refactor, it's gonna be provided by IoC {@link
     *     TlsConfigurationSetter}
     */
    @Deprecated
    protected void configureTls(
            UkOpenBankingClientConfigurationAdapter ukOpenBankingConfiguration) {
        if (tlsConfigurationSetter != null) {
            tlsConfigurationSetter.applyConfiguration(client);
        } else if (getClientConfigurationFormat().equals(UkOpenBankingConfiguration.class)) {
            UkOpenBankingConfiguration configuration =
                    (UkOpenBankingConfiguration) ukOpenBankingConfiguration;
            tlsConfigurationSetter =
                    new LocalCertificateTlsConfigurationSetter(
                            configuration.getTransportKey(),
                            configuration.getTransportKeyPassword());
            tlsConfigurationSetter.applyConfiguration(client);
        } else {
            client.setEidasProxy(configuration.getEidasProxy());
        }
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

    @Override
    public FetchTransferDestinationsResponse fetchBeneficiaries(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    protected TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController,
                new UkOpenBankingTransferDestinationFetcher(
                        new NoOpTransferDestinationAccountsProvider(),
                        AccountIdentifierType.SORT_CODE,
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
                .makePartyFetcher(apiClient)
                .fetchParty()
                .map(PartyMapper::toIdentityData)
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

    private OpenIdAuthenticationController createUkOpenBankingAuthenticationController() {
        return new OpenIdAuthenticationController(
                this.persistentStorage,
                this.supplementalInformationHelper,
                this.apiClient,
                getAisAuthenticator(),
                this.credentials,
                this.strongAuthenticationState,
                this.request.getCallbackUri(),
                this.randomValueGenerator,
                new OpenIdAuthenticationValidator(this.apiClient));
    }

    protected OpenIdAuthenticator getAisAuthenticator() {
        return new OpenIdAisAuthenticator(this.apiClient);
    }

    @Override
    public Optional<PaymentController> getPaymentController(Payment payment) {
        if (paymentController == null) {
            paymentController = constructPaymentController(payment).orElse(null);
        }

        return Optional.ofNullable(paymentController);
    }

    public Optional<PaymentController> constructPaymentController(Payment payment) {
        if (this.pisConfig == null || this.pisRequestFilter == null) {
            return Optional.empty();
        }

        UkOpenBankingPaymentStorage paymentStorage = pisRequestFilter.getStorage();
        UkOpenBankingPisAuthApiClient authApiClient = createAuthApiClient(paymentStorage);
        OpenIdAuthenticationValidator authenticationValidator =
                new OpenIdAuthenticationValidator(authApiClient);
        UkOpenBankingPisAuthFilterInstantiator authFilterInstantiator =
                new UkOpenBankingPisAuthFilterInstantiator(
                        authApiClient, authenticationValidator, paymentStorage);
        UkOpenBankingPaymentAuthenticator paymentAuthenticator =
                createPaymentAuthenticator(authApiClient, authenticationValidator);

        UkOpenBankingRequestBuilder requestBuilder =
                new UkOpenBankingRequestBuilder(client, pisRequestFilter);
        UkOpenBankingPaymentApiClient paymentApiClient =
                createPaymentApiClient(requestBuilder, payment);

        UkOpenBankingCredentialsUpdater credentialsUpdater =
                new UkOpenBankingCredentialsUpdater(credentials, systemUpdater);

        UkOpenBankingPaymentExecutor paymentExecutor =
                new UkOpenBankingPaymentExecutor(
                        paymentApiClient,
                        paymentAuthenticator,
                        authFilterInstantiator,
                        getPaymentRequestValidator(),
                        providerSessionCacheController,
                        credentialsUpdater);

        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }

    private UkOpenBankingPisAuthApiClient createAuthApiClient(
            UkOpenBankingPaymentStorage paymentStorage) {
        return new UkOpenBankingPisAuthApiClient(
                client,
                jwtSigner,
                softwareStatement,
                redirectUrl,
                providerConfiguration,
                randomValueGenerator,
                pisConfig,
                paymentStorage);
    }

    private UkOpenBankingPaymentAuthenticator createPaymentAuthenticator(
            UkOpenBankingPisAuthApiClient paymentApiClient,
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

    private UkOpenBankingPaymentApiClient createPaymentApiClient(
            UkOpenBankingRequestBuilder requestBuilder, Payment payment) {
        final PaymentType paymentType = UkOpenBankingPaymentHelper.getPaymentType(payment);

        switch (paymentType) {
            case DOMESTIC:
            case SEPA:
                return new DomesticPaymentApiClient(
                        requestBuilder, getDomesticPaymentConverter(), pisConfig.getBaseUrl());
            case DOMESTIC_FUTURE:
                return new DomesticScheduledPaymentApiClient(
                        requestBuilder,
                        getDomesticScheduledPaymentConverter(),
                        pisConfig.getBaseUrl());
            default:
                throw new IllegalArgumentException(
                        (String.format("Unknown type: %s", paymentType)));
        }
    }

    protected UkOpenBankingPaymentRequestValidator getPaymentRequestValidator() {
        return new DefaultUkOpenBankingPaymentRequestValidator();
    }

    protected DomesticPaymentConverter getDomesticPaymentConverter() {
        return new DomesticPaymentConverter();
    }

    protected DomesticScheduledPaymentConverter getDomesticScheduledPaymentConverter() {
        return new DomesticScheduledPaymentConverter();
    }

    private void setSoftwareStatementForSignatureCreator(
            SoftwareStatementAssertion softwareStatement) {
        if (Objects.nonNull(pisRequestFilter)) {
            pisRequestFilter.setSoftwareStatement(softwareStatement);
        }
    }

    protected static UkOpenBankingPisRequestFilter createPisRequestFilterUsingPs256Base64Signature(
            JwtSigner jwtSigner, RandomValueGenerator randomValueGenerator) {
        return createPisRequestFilter(
                new UkOpenBankingPs256Base64SignatureCreator(jwtSigner),
                jwtSigner,
                randomValueGenerator);
    }

    protected static UkOpenBankingPisRequestFilter
            createPisRequestFilterUsingPs256WithoutBase64Signature(
                    JwtSigner jwtSigner, RandomValueGenerator randomValueGenerator) {
        return createPisRequestFilter(
                new UkOpenBankingPs256WithoutBase64SignatureCreator(jwtSigner),
                jwtSigner,
                randomValueGenerator);
    }

    protected static UkOpenBankingPisRequestFilter createPisRequestFilterUsingPs256MinimalSignature(
            JwtSigner jwtSigner, RandomValueGenerator randomValueGenerator) {
        return createPisRequestFilter(
                new UkOpenBankingPs256MinimalSignatureCreator(jwtSigner),
                jwtSigner,
                randomValueGenerator);
    }

    protected static UkOpenBankingPisRequestFilter createPisRequestFilter(
            UkOpenBankingPs256SignatureCreator ps256SignatureCreator,
            JwtSigner jwtSigner,
            RandomValueGenerator randomValueGenerator) {
        final UkOpenBankingPaymentStorage paymentStorage = new UkOpenBankingPaymentStorage();
        final UkOpenBankingJwtSignatureHelper jwtSignatureHelper =
                new UkOpenBankingJwtSignatureHelper(
                        new ObjectMapper(),
                        paymentStorage,
                        new UkOpenBankingRs256SignatureCreator(jwtSigner),
                        ps256SignatureCreator);

        return new UkOpenBankingPisRequestFilter(
                jwtSignatureHelper, paymentStorage, randomValueGenerator);
    }

    private static void configureMdcPropagation() {
        RxJavaPlugins.setScheduleHandler(RunnableMdcWrapper::wrap);
    }
}
