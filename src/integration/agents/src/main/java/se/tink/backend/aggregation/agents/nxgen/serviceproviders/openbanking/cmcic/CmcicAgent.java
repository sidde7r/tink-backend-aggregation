package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic;

import java.util.Collection;
import java.util.List;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient.CmcicApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient.CmcicRepository;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient.CmcicRequestValuesProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.CmcicAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration.CmcicAgentConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration.CmcicConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.CmcicPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.CmcicPaymentRequestFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.CmcicPaymentResponseMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.creditcard.CmcicCreditCardConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.creditcard.CmcicCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.CmcicIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.CmcicTransactionalAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.CmcicTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transfer.CmcicTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.provider.CmcicCodeChallengeProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.provider.CmcicDigestProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.provider.CmcicSignatureProvider;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TerminatedHandshakeRetryFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;

public abstract class CmcicAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor,
                RefreshBeneficiariesExecutor,
                RefreshCreditCardAccountsExecutor {

    protected final CmcicApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final AgentConfiguration<CmcicConfiguration> agentConfiguration;
    private final CmcicIdentityDataFetcher cmcicIdentityDataFetcher;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final CmcicRepository cmcicRepository;

    protected CmcicAgent(AgentComponentProvider componentProvider, CmcicAgentConfig agentConfig) {
        super(componentProvider);

        this.agentConfiguration =
                getAgentConfigurationController().getAgentConfiguration(CmcicConfiguration.class);

        final CmcicSignatureProvider signatureProvider =
                new CmcicSignatureProvider(componentProvider.getQsealcSigner());
        final CmcicDigestProvider digestProvider = new CmcicDigestProvider();
        final CmcicCodeChallengeProvider codeChallengeProvider = new CmcicCodeChallengeProvider();

        this.client.setResponseStatusHandler(new CmicResponseStatusHandler());
        this.client.addFilter(new TimeoutFilter());
        this.client.addFilter(new TerminatedHandshakeRetryFilter());
        this.cmcicRepository = new CmcicRepository(this.persistentStorage, this.sessionStorage);

        this.apiClient =
                new CmcicApiClient(
                        this.client,
                        this.cmcicRepository,
                        this.agentConfiguration,
                        digestProvider,
                        signatureProvider,
                        codeChallengeProvider,
                        agentConfig,
                        new CmcicRequestValuesProvider(
                                componentProvider.getRandomValueGenerator(),
                                componentProvider.getLocalDateTimeSource(),
                                agentConfiguration.getQsealc()));
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        this.cmcicIdentityDataFetcher = new CmcicIdentityDataFetcher(this.apiClient);
        this.transferDestinationRefreshController = constructTransferDestinationRefreshController();
        this.creditCardRefreshController = constructCreditCardRefreshController();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        return Optional.of(
                new PaymentController(
                        new CmcicPaymentExecutor(
                                apiClient,
                                cmcicRepository,
                                agentConfiguration,
                                supplementalInformationHelper,
                                strongAuthenticationState,
                                new CmcicPaymentRequestFactory(),
                                new CmcicPaymentResponseMapper())));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new CmcicAuthenticator(apiClient, persistentStorage),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
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

    private CreditCardRefreshController constructCreditCardRefreshController() {
        CmcicCreditCardFetcher creditCardFetcher =
                new CmcicCreditCardFetcher(apiClient, new CmcicCreditCardConverter());
        return new CreditCardRefreshController(
                this.metricRefreshController,
                this.updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(creditCardFetcher)));
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final CmcicTransactionalAccountFetcher accountFetcher =
                new CmcicTransactionalAccountFetcher(
                        apiClient, new CmcicTransactionalAccountConverter());

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(accountFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return cmcicIdentityDataFetcher.response();
    }

    @Override
    public FetchTransferDestinationsResponse fetchBeneficiaries(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    private TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new CmcicTransferDestinationFetcher(apiClient));
    }

    protected static String getPreferredLanguage(
            String locale, Collection<String> supportedLanguages, String primaryLanguage) {
        for (String lang : supportedLanguages) {
            if (locale.startsWith(lang)) {
                return lang;
            }
        }
        return primaryLanguage;
    }
}
