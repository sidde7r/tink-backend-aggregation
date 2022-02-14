package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.OpBankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.configuration.OpBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.OpBankPaymentController;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.OpBankPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.OpBankTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.creditcard.OpBankCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.transactionalaccount.OpBankTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.filter.OpBankServiceUnavailableFilter;
import se.tink.backend.aggregation.agents.tools.jwt.JwksClient;
import se.tink.backend.aggregation.agents.tools.jwt.kid.JwksKeyIdProvider;
import se.tink.backend.aggregation.agents.tools.jwt.kid.KeyIdProvider;
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
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS, CREDIT_CARDS, SAVINGS_ACCOUNTS, TRANSFERS})
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER,
            PisCapability.PIS_SEPA_RECURRING_PAYMENTS
        })
public final class OpBankAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshTransferDestinationExecutor {

    private final OpBankApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final TransferDestinationRefreshController transferDestinationController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final AgentComponentProvider agentComponentProvider;

    @Inject
    public OpBankAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);
        this.agentComponentProvider = componentProvider;
        client.addFilter(new OpBankServiceUnavailableFilter());
        apiClient =
                new OpBankApiClient(
                        client,
                        persistentStorage,
                        getAgentConfiguration(),
                        qsealcSigner,
                        componentProvider.getUser());
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
        creditCardRefreshController = constructCreditCardRefreshController();
        transferDestinationController = constructTransferDestinationController();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    protected AgentConfiguration<OpBankConfiguration> getAgentConfiguration() {
        try {
            return getAgentConfigurationController()
                    .getAgentConfiguration(OpBankConfiguration.class);
        } catch (IllegalStateException e) {
            throw new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION);
        }
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        final OpBankCreditCardAccountFetcher creditCardFetcher =
                new OpBankCreditCardAccountFetcher(apiClient);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(creditCardFetcher)));
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        final OpBankTransactionalAccountFetcher accountFetcher =
                new OpBankTransactionalAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(accountFetcher)));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        KeyIdProvider keyIdProvider =
                new JwksKeyIdProvider(
                        new JwksClient(agentComponentProvider.getTinkHttpClient()),
                        getAgentConfiguration()
                                .getProviderSpecificConfiguration()
                                .getJwksEndpoint(),
                        extractQSealCertificate());
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new OpBankAuthenticator(
                                apiClient,
                                persistentStorage,
                                credentials,
                                getAgentConfiguration(),
                                keyIdProvider),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    @SneakyThrows
    private X509Certificate extractQSealCertificate() {
        return CertificateUtils.getX509CertificatesFromBase64EncodedCert(
                        getAgentConfiguration().getQsealc())
                .stream()
                .findFirst() // first from the chain will always be the right one
                .orElseThrow(() -> new CertificateException("No certificate found"));
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
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
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
    public Optional<PaymentController> constructPaymentController() {
        KeyIdProvider keyIdProvider =
                new JwksKeyIdProvider(
                        new JwksClient(agentComponentProvider.getTinkHttpClient()),
                        getAgentConfiguration()
                                .getProviderSpecificConfiguration()
                                .getJwksEndpoint(),
                        extractQSealCertificate());
        return Optional.of(
                new OpBankPaymentController(
                        new OpBankPaymentExecutor(
                                apiClient,
                                getAgentConfiguration(),
                                credentials,
                                keyIdProvider,
                                persistentStorage),
                        supplementalInformationHelper,
                        strongAuthenticationState,
                        persistentStorage));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationController.fetchTransferDestinations(accounts);
    }

    private TransferDestinationRefreshController constructTransferDestinationController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new OpBankTransferDestinationFetcher());
    }
}
