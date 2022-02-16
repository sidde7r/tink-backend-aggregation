package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.util.Optional;
import lombok.SneakyThrows;
import se.tink.agent.sdk.operation.Provider;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient.BpceGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient.BpceRateLimitFilter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient.BpceResponseHandler;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.BpceGroupAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.configuration.BpceGroupConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.creditcard.BpceGroupCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.BpceGroupTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactions.BpceGroupBaseTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.payment.BpceGroupPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.payment.BpceGroupPaymentDatePolicy;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.signature.BpceGroupRequestSigner;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.signature.BpceGroupSignatureHeaderGenerator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.storage.BpceOAuth2TokenStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingRequestValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.validator.FrCreatePaymentRequestValidator;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.LoggingStrategy;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS, CREDIT_CARDS, TRANSFERS})
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER
        })
public final class BpceGroupAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshCreditCardAccountsExecutor {

    private final BpceGroupApiClient bpceGroupApiClient;
    private final BpceGroupPaymentApiClient bpceGroupPaymentApiClient;
    private final BpceOAuth2TokenStorage bpceOAuth2TokenStorage;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final Provider provider;

    @Inject
    @SneakyThrows
    public BpceGroupAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);

        setJsonHttpTrafficLogsEnabled(true);
        client.setLoggingStrategy(LoggingStrategy.EXPERIMENTAL);

        final AgentConfiguration<BpceGroupConfiguration> agentConfiguration =
                getAgentConfiguration();
        final BpceGroupConfiguration bpceGroupConfiguration =
                agentConfiguration.getProviderSpecificConfiguration();
        bpceGroupConfiguration.setClientId(
                CertificateUtils.getOrganizationIdentifier(agentConfiguration.getQsealc()));
        provider = componentProvider.getProvider();
        bpceGroupConfiguration.setServerUrl(provider.getPayload());
        final BpceGroupSignatureHeaderGenerator bpceGroupSignatureHeaderGenerator =
                createSignatureHeaderGenerator(qsealcSigner, bpceGroupConfiguration);
        final String redirectUrl = agentConfiguration.getRedirectUrl();

        this.bpceOAuth2TokenStorage = new BpceOAuth2TokenStorage(this.persistentStorage);

        this.client.setResponseStatusHandler(new BpceResponseHandler());
        this.client.addFilter(new BpceRateLimitFilter(provider.getName()));

        this.bpceGroupApiClient =
                new BpceGroupApiClient(
                        this.client,
                        this.bpceOAuth2TokenStorage,
                        bpceGroupConfiguration,
                        redirectUrl,
                        bpceGroupSignatureHeaderGenerator,
                        componentProvider.getUser().getIpAddress(),
                        new TemporaryStorage());

        this.bpceGroupPaymentApiClient =
                new BpceGroupPaymentApiClient(
                        client,
                        sessionStorage,
                        agentConfiguration.getProviderSpecificConfiguration(),
                        bpceGroupSignatureHeaderGenerator,
                        new FrCreatePaymentRequestValidator(
                                componentProvider.getLocalDateTimeSource()));

        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();

        this.creditCardRefreshController = getCreditCardRefreshController();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new BpceGroupAuthenticator(bpceGroupApiClient, bpceOAuth2TokenStorage),
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
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    @Override
    public SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    private BpceGroupSignatureHeaderGenerator createSignatureHeaderGenerator(
            QsealcSigner qsealcSigner, BpceGroupConfiguration bpceGroupConfiguration) {

        final BpceGroupRequestSigner bpceGroupRequestSigner =
                new BpceGroupRequestSigner(qsealcSigner);

        return new BpceGroupSignatureHeaderGenerator(
                bpceGroupConfiguration, bpceGroupRequestSigner);
    }

    private AgentConfiguration<BpceGroupConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfiguration(BpceGroupConfiguration.class);
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {

        final BpceGroupTransactionalAccountFetcher accountFetcher =
                new BpceGroupTransactionalAccountFetcher(bpceGroupApiClient);

        final BpceGroupBaseTransactionsFetcher<TransactionalAccount> transactionFetcher =
                new BpceGroupBaseTransactionsFetcher<>(bpceGroupApiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(transactionFetcher, 1)));
    }

    private CreditCardRefreshController getCreditCardRefreshController() {
        final BpceGroupCreditCardFetcher creditCardFetcher =
                new BpceGroupCreditCardFetcher(bpceGroupApiClient);

        final BpceGroupBaseTransactionsFetcher<CreditCardAccount> transactionFetcher =
                new BpceGroupBaseTransactionsFetcher<>(bpceGroupApiClient);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(transactionFetcher, 1)));
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        final AgentConfiguration<BpceGroupConfiguration> agentConfiguration =
                getAgentConfiguration();

        FrOpenBankingPaymentExecutor paymentExecutor =
                new FrOpenBankingPaymentExecutor(
                        bpceGroupPaymentApiClient,
                        agentConfiguration.getRedirectUrl(),
                        sessionStorage,
                        strongAuthenticationState,
                        supplementalInformationHelper,
                        new BpceGroupPaymentDatePolicy(provider.getName()),
                        new FrOpenBankingRequestValidator(provider.getName()));

        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }
}
