package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.UkOpenBankingAisAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.UkOpenBankingAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.UkOpenBankingTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.session.UkOpenBankingSessionHandler;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdAuthenticationFlow;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.serialization.utils.SerializationUtils;

public abstract class UkOpenBankingBaseAgent extends NextGenerationAgent
        implements RefreshTransferDestinationExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    private final Provider tinkProvider;
    private final URL wellKnownURL;
    // Separate httpClient used for payments since PIS and AIS are two different
    // authenticated flows.
    private final TinkHttpClient paymentsHttpClient;
    protected UkOpenBankingApiClient apiClient;
    protected SoftwareStatement softwareStatement;
    protected ProviderConfiguration providerConfiguration;
    private boolean disableSslVerification;

    private TransferDestinationRefreshController transferDestinationRefreshController;
    private CreditCardRefreshController creditCardRefreshController;
    private TransactionalAccountRefreshController transactionalAccountRefreshController;

    // Lazy loaded
    private UkOpenBankingAis aisSupport;
    private UkOpenBankingAccountFetcher<?, ?, TransactionalAccount> transactionalAccountFetcher;

    public UkOpenBankingBaseAgent(
            CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair,
            URL wellKnownURL) {
        this(request, context, signatureKeyPair, wellKnownURL, false);
    }

    public UkOpenBankingBaseAgent(
            CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair,
            URL wellKnownURL,
            boolean disableSslVerification) {

        super(request, context, signatureKeyPair);
        this.disableSslVerification = disableSslVerification;

        this.paymentsHttpClient =
                new TinkHttpClient(
                        context.getAggregatorInfo(),
                        metricContext.getMetricRegistry(),
                        context.getLogOutputStream(),
                        signatureKeyPair,
                        request.getProvider());
        tinkProvider = request.getProvider();
        this.wellKnownURL = wellKnownURL;
    }

    private String getSoftwareStatementName() {
        return tinkProvider.getPayload().split(":")[0];
    }

    private String getProviderName() {
        return tinkProvider.getPayload().split(":")[1];
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        UkOpenBankingConfiguration ukOpenBankingConfiguration =
                configuration
                        .getIntegrations()
                        .getIntegration(UkOpenBankingConstants.INTEGRATION_NAME, String.class)
                        .map(
                                s ->
                                        SerializationUtils.deserializeFromString(
                                                s, UkOpenBankingConfiguration.class))
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "UK Open Banking integration not configured"));

        String softwareStatementName = getSoftwareStatementName();
        String providerName = getProviderName();

        softwareStatement =
                ukOpenBankingConfiguration
                        .getSoftwareStatement(softwareStatementName)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                String.format(
                                                        "Could not find softwareStatement: %s",
                                                        softwareStatementName)));

        providerConfiguration =
                softwareStatement
                        .getProviderConfiguration(providerName)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                String.format(
                                                        "Could not find provider conf: %s",
                                                        providerName)));

        if (this.disableSslVerification) {
            client.disableSslVerification();
            paymentsHttpClient.disableSslVerification();
        } else {
            client.trustRootCaCertificate(
                    ukOpenBankingConfiguration.getRootCAData(),
                    ukOpenBankingConfiguration.getRootCAPassword());

            paymentsHttpClient.trustRootCaCertificate(
                    ukOpenBankingConfiguration.getRootCAData(),
                    ukOpenBankingConfiguration.getRootCAPassword());
        }

        apiClient = createApiClient(client, softwareStatement, providerConfiguration);

        // -    We cannot configure the paymentsHttpClient from `configureHttpClient()` because it
        // will be null
        //      at that stage.
        // -    Some banks are extremely slow at PIS operations (esp. the payment submission step),
        // increase the the
        //      timeout on that http client.
        int timeoutInMilliseconds = (int) TimeUnit.SECONDS.toMillis(120);
        paymentsHttpClient.setTimeout(timeoutInMilliseconds);

        configureAisHttpClient(client);
        configurePisHttpClient(paymentsHttpClient);

        this.transferDestinationRefreshController = constructTransferDestinationRefreshController();

        this.creditCardRefreshController = constructCreditCardRefreshController();

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    protected UkOpenBankingApiClient createApiClient(
            TinkHttpClient httpClient,
            SoftwareStatement softwareStatement,
            ProviderConfiguration providerConfiguration) {
        return new UkOpenBankingApiClient(
                httpClient, softwareStatement, providerConfiguration, wellKnownURL);
    }

    @Override
    protected abstract Authenticator constructAuthenticator();

    protected Authenticator constructAuthenticator(UkOpenBankingAisConfig aisConfig) {
        UkOpenBankingAisAuthenticator authenticator =
                new UkOpenBankingAisAuthenticator(apiClient, aisConfig);
        return createOpenIdFlowWithAuthenticator(authenticator, null);
    }

    protected Authenticator constructAuthenticator(
            UkOpenBankingAisConfig aisConfig, URL appToAppRedirectURL) {
        UkOpenBankingAisAuthenticator authenticator =
                new UkOpenBankingAisAuthenticator(apiClient, aisConfig);
        return createOpenIdFlowWithAuthenticator(authenticator, appToAppRedirectURL);
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
                appToAppRedirectURL);
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

    protected abstract void configureAisHttpClient(TinkHttpClient httpClient);

    protected abstract void configurePisHttpClient(TinkHttpClient httpClient);
}
