package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LIST_BENEFICIARIES;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.time.Clock;
import java.time.ZoneId;
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
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclHeaderValueProvider;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclTokenApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.authenticator.LclAccessTokenProvider;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.authenticator.LclThirdPartyAppRequestParamsProvider;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.configuration.LclConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.account.LclAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.converter.LclDataConverter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.creditcard.LclCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.identity.LclIdentityFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.transaction.LclTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.transferdestination.LclTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.payment.LclPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.signature.LclSignatureProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentExecutor;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProviderImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2TokenFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2TokenStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessCodeStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenFetchHelper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.OAuth2BasedTokenAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.ThirdPartyAppCallbackProcessor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.TokenLifeTime;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.steps.ThirdPartyAppAuthenticationStepCreator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS, IDENTITY_DATA, LIST_BENEFICIARIES, TRANSFERS, CREDIT_CARDS})
public final class LclAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshIdentityDataExecutor,
                RefreshBeneficiariesExecutor,
    RefreshCreditCardAccountsExecutor {

    private static final ZoneId ZONE_ID = ZoneId.of("CET");
    public static final String BASE_URL = "https://psd.lcl.fr";
    public static final String CLIENT_ID = "PSDSE-FINA-44059_1";

    private final LclApiClient lclApiClient;
    private final LclPaymentApiClient paymentApiClient;
    private final LclTokenApiClient tokenApiClient;
    private final OAuth2TokenStorage tokenStorage;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final LclIdentityFetcher lclIdentityFetcher;
    private final AgentConfiguration<LclConfiguration> agentConfiguration;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final LclDataConverter dataConverter;

    @Inject
    public LclAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);

        this.agentConfiguration = getAgentConfiguration();
        this.tokenStorage = new OAuth2TokenStorage(this.persistentStorage, this.sessionStorage);

        LclHeaderValueProvider lclHeaderValueProvider = getLclHeaderValueProvider(qsealcSigner);
        this.tokenApiClient =
                new LclTokenApiClient(this.client, lclHeaderValueProvider, this.agentConfiguration);
        this.lclApiClient =
                new LclApiClient(this.client, lclHeaderValueProvider, this.tokenStorage);
        this.paymentApiClient =
                new LclPaymentApiClient(
                        this.client,
                        lclHeaderValueProvider,
                        this.sessionStorage,
                        this.tokenApiClient);

        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        this.creditCardRefreshController = constructCreditCardRefreshController();
        this.lclIdentityFetcher = new LclIdentityFetcher(this.lclApiClient);
        this.transferDestinationRefreshController = constructTransferDestinationRefreshController();
        this.dataConverter = new LclDataConverter();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        this.client.setEidasProxy(configuration.getEidasProxy());
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        final LclAccessTokenProvider accessTokenProvider =
                new LclAccessTokenProvider(this.tokenApiClient);

        final AccessTokenFetchHelper<OAuth2Token> accessTokenFetchHelper =
                new AccessTokenFetchHelper<>(
                        accessTokenProvider,
                        this.credentials,
                        new TokenLifeTime(
                                AccessTokenFetchHelper.DEFAULT_TOKEN_LIFETIME,
                                AccessTokenFetchHelper.DEFAULT_TOKEN_LIFETIME_UNIT));

        final AccessCodeStorage accessCodeStorage = new AccessCodeStorage(this.sessionStorage);

        final AccessTokenFetcher accessTokenFetcher =
                new OAuth2TokenFetcher(
                        accessTokenFetchHelper, this.tokenStorage, accessCodeStorage);

        final LclThirdPartyAppRequestParamsProvider thirdPartyAppRequestParamsProvider =
                new LclThirdPartyAppRequestParamsProvider(this.agentConfiguration);

        final ThirdPartyAppCallbackProcessor thirdPartyAppCallbackProcessor =
                new ThirdPartyAppCallbackProcessor(thirdPartyAppRequestParamsProvider);

        final ThirdPartyAppAuthenticationStepCreator thirdPartyAppAuthenticationStepCreator =
                new ThirdPartyAppAuthenticationStepCreator(
                        thirdPartyAppCallbackProcessor,
                        accessCodeStorage,
                        this.strongAuthenticationState);

        return new OAuth2BasedTokenAuthenticator(
                accessTokenFetcher, thirdPartyAppAuthenticationStepCreator);
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return this.transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return this.transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return this.lclIdentityFetcher.response();
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return this.creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return this.creditCardRefreshController.fetchCreditCardTransactions();
    }

    @Override
    public FetchTransferDestinationsResponse fetchBeneficiaries(List<Account> accounts) {
        /* TODO: In the future add dedicated RefreshController for beneficiary listing. */
        return this.transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        final SupplementalInformationProvider supplementalInformationProvider =
                new SupplementalInformationProviderImpl(supplementalRequester, request);

        FrOpenBankingPaymentExecutor paymentExecutor =
                new FrOpenBankingPaymentExecutor(
                        paymentApiClient,
                        agentConfiguration.getRedirectUrl(),
                        sessionStorage,
                        strongAuthenticationState,
                        supplementalInformationProvider.getSupplementalInformationHelper());

        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {

        LclCreditCardFetcher lclCreditCardFetcher =
                new LclCreditCardFetcher(this.lclApiClient, this.dataConverter);
        return new CreditCardRefreshController(
                this.metricRefreshController,
                this.updateController,
                lclCreditCardFetcher,
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionPagePaginationController<>(lclCreditCardFetcher, 1)));
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final PrioritizedValueExtractor prioritizedValueExtractor = new PrioritizedValueExtractor();
        final LclAccountFetcher accountFetcher =
                new LclAccountFetcher(this.lclApiClient, prioritizedValueExtractor, dataConverter);
        final LclTransactionFetcher transactionFetcher =
                new LclTransactionFetcher(this.lclApiClient, dataConverter);

        return new TransactionalAccountRefreshController(
                this.metricRefreshController,
                this.updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionPagePaginationController<>(transactionFetcher, 1)));
    }

    private TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                this.metricRefreshController, new LclTransferDestinationFetcher(lclApiClient));
    }

    private AgentConfiguration<LclConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController().getAgentConfiguration(LclConfiguration.class);
    }

    private LclHeaderValueProvider getLclHeaderValueProvider(QsealcSigner qsealcSigner) {
        final LclSignatureProvider signatureProvider = new LclSignatureProvider(qsealcSigner);
        final Clock clock = Clock.system(ZONE_ID);
        return new LclHeaderValueProvider(
                signatureProvider,
                this.agentConfiguration.getProviderSpecificConfiguration(),
                clock);
    }
}
