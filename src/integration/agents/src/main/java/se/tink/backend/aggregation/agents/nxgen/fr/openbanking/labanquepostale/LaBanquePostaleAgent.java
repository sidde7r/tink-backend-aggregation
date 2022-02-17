package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.LIST_BENEFICIARIES;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshBeneficiariesExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.LaBanquePostaleAccountSegmentSpecifier;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.LaBanquePostaleAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.LaBanquePostaleOAuth2AuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.configuration.LaBanquePostaleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.card.LaBanquePostaleCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.card.LaBanquePostaleCreditCardConverter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.identity.LaBanquePostaleIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transaction.LaBanquePostaleTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.LaBanquePostaleTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.converter.LaBanquePostaleTransactionalAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transfer.LaBanquePostaleTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupAgent;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.logmasker.LogMasker;
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
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.LoggingStrategy;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TerminatedHandshakeRetryFilter;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS, IDENTITY_DATA, LIST_BENEFICIARIES, TRANSFERS, CREDIT_CARDS})
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER,
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.PIS_FUTURE_DATE,
        })
public final class LaBanquePostaleAgent
        extends BerlinGroupAgent<LaBanquePostaleApiClient, LaBanquePostaleConfiguration>
        implements RefreshIdentityDataExecutor,
                RefreshBeneficiariesExecutor,
                RefreshCreditCardAccountsExecutor {

    private final QsealcSigner qsealcSigner;
    private final LaBanquePostaleIdentityDataFetcher identityDataFetcher;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final LaBanquePostalePaymentApiClient paymentApiClient;
    private final CreditCardRefreshController creditCardRefreshController;
    private final AgentConfiguration<LaBanquePostaleConfiguration> agentConfiguration;
    private final LaBanquePostaleTransactionalAccountConverter transactionalAccountConverter;
    private final LaBanquePostaleCreditCardConverter creditCardConverter;
    private final LaBanquePostaleUserState userState;
    private final LaBanquePostaleAccountSegmentSpecifier accountSegmentSpecifier;
    private final LogMasker logMasker;

    @Inject
    public LaBanquePostaleAgent(
            AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);

        setJsonHttpTrafficLogsEnabled(true);
        client.setLoggingStrategy(LoggingStrategy.EXPERIMENTAL);

        this.logMasker = componentProvider.getContext().getLogMasker();
        client.addFilter(new TerminatedHandshakeRetryFilter());
        client.addFilter(new LaBanquePostaleRetryFilter());
        client.setResponseStatusHandler(new LaBanquePostaleResponseErrorHandler());
        agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(LaBanquePostaleConfiguration.class);

        this.qsealcSigner = qsealcSigner;
        this.userState = new LaBanquePostaleUserState(getPersistentStorage());
        this.accountSegmentSpecifier =
                new LaBanquePostaleAccountSegmentSpecifier(
                        supplementalInformationHelper, userState, request);
        this.apiClient = createApiClient();
        this.transactionalAccountConverter = new LaBanquePostaleTransactionalAccountConverter();
        this.creditCardConverter = new LaBanquePostaleCreditCardConverter();
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        this.identityDataFetcher = new LaBanquePostaleIdentityDataFetcher(this.apiClient);
        this.transferDestinationRefreshController = constructTransferDestinationRefreshController();
        this.paymentApiClient =
                new LaBanquePostalePaymentApiClient(
                        client, sessionStorage, agentConfiguration, qsealcSigner);
        this.creditCardRefreshController = constructCardController();
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
    public FetchIdentityDataResponse fetchIdentityData() {
        return identityDataFetcher.response();
    }

    @Override
    public FetchTransferDestinationsResponse fetchBeneficiaries(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        LaBanquePostalePaymentExecutor paymentExecutor =
                new LaBanquePostalePaymentExecutor(
                        paymentApiClient,
                        agentConfiguration.getRedirectUrl(),
                        strongAuthenticationState,
                        supplementalInformationHelper);
        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }

    @Override
    protected LaBanquePostaleApiClient createApiClient() {
        return new LaBanquePostaleApiClient(
                client,
                persistentStorage,
                qsealcSigner,
                getConfiguration().getProviderSpecificConfiguration(),
                request,
                getConfiguration().getRedirectUrl(),
                getConfiguration().getQsealc(),
                userState,
                logMasker);
    }

    @Override
    protected Class<LaBanquePostaleConfiguration> getConfigurationClassDescription() {
        return LaBanquePostaleConfiguration.class;
    }

    @Override
    protected Authenticator constructAuthenticator() {
        accountSegmentSpecifier.specifyAccountSegment();
        final OAuth2AuthenticationController oAuth2Authenticator =
                new LaBanquePostaleOAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new LaBanquePostaleAuthenticator(apiClient),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        oAuth2Authenticator, supplementalInformationHelper),
                oAuth2Authenticator);
    }

    @Override
    protected TransactionalAccountRefreshController getTransactionalAccountRefreshController() {

        LaBanquePostaleTransactionalAccountFetcher accountFetcher =
                new LaBanquePostaleTransactionalAccountFetcher(
                        apiClient, transactionalAccountConverter);
        LaBanquePostaleTransactionFetcher<TransactionalAccount> transactionFetcher =
                new LaBanquePostaleTransactionFetcher<>(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }

    private TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new LaBanquePostaleTransferDestinationFetcher(apiClient));
    }

    private CreditCardRefreshController constructCardController() {
        LaBanquePostaleCardFetcher laBanquePostaleCardFetcher =
                new LaBanquePostaleCardFetcher(apiClient, creditCardConverter);

        LaBanquePostaleTransactionFetcher<CreditCardAccount> transactionFetcher =
                new LaBanquePostaleTransactionFetcher<>(apiClient);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                laBanquePostaleCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }
}
