package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale;

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
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.RetryFilter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.authenticator.SocieteGeneraleAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.configuration.SocieteGeneraleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.SocieteGeneralePaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.card.SocieteGeneraleCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.identity.SocieteGeneraleIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.SocieteGeneraleTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactions.SocieteGeneraleTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transfer.SocieteGeneraleTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.filters.SocieteGeneraleRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.utils.SignatureHeaderProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.validator.FrCreatePaymentRequestValidator;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
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
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.LoggingStrategy;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS, LIST_BENEFICIARIES, IDENTITY_DATA, TRANSFERS, CREDIT_CARDS})
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER,
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.PIS_FUTURE_DATE,
        })
public final class SocieteGeneraleAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshIdentityDataExecutor,
                RefreshBeneficiariesExecutor,
                RefreshCreditCardAccountsExecutor {

    private final SocieteGeneraleApiClient apiClient;
    private final SocieteGeneraleAuthenticator authenticator;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final SocieteGeneraleIdentityDataFetcher societeGeneraleIdentityDataFetcher;
    private final AgentConfiguration<SocieteGeneraleConfiguration> agentConfiguration;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final LocalDateTimeSource localDateTimeSource;

    @Inject
    public SocieteGeneraleAgent(
            AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);

        setJsonHttpTrafficLogsEnabled(true);
        client.setLoggingStrategy(LoggingStrategy.EXPERIMENTAL);

        agentConfiguration = getAgentConfiguration();
        localDateTimeSource = componentProvider.getLocalDateTimeSource();

        final SocieteGeneraleConfiguration societeGeneraleConfiguration =
                agentConfiguration.getProviderSpecificConfiguration();
        final SignatureHeaderProvider signatureHeaderProvider =
                new SignatureHeaderProvider(qsealcSigner, societeGeneraleConfiguration);

        apiClient =
                new SocieteGeneraleApiClient(
                        client,
                        persistentStorage,
                        societeGeneraleConfiguration,
                        agentConfiguration.getRedirectUrl(),
                        signatureHeaderProvider);

        authenticator =
                new SocieteGeneraleAuthenticator(apiClient, persistentStorage, agentConfiguration);

        societeGeneraleIdentityDataFetcher = new SocieteGeneraleIdentityDataFetcher(apiClient);

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();

        creditCardRefreshController = constructCreditCardRefresher();

        transferDestinationRefreshController = constructTransferDestinationRefreshController();
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
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
        client.addFilter(
                new SocieteGeneraleRetryFilter(
                        RetryFilter.NUM_TIMEOUT_RETRIES, RetryFilter.RETRY_SLEEP_MILLISECONDS));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        authenticator,
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
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
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(
                societeGeneraleIdentityDataFetcher.fetchIdentityData());
    }

    @Override
    public FetchTransferDestinationsResponse fetchBeneficiaries(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        SocieteGeneraleTransactionalAccountFetcher accountFetcher =
                new SocieteGeneraleTransactionalAccountFetcher(apiClient);

        SocieteGeneraleTransactionFetcher<TransactionalAccount> transactionFetcher =
                new SocieteGeneraleTransactionFetcher<>(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }

    private CreditCardRefreshController constructCreditCardRefresher() {
        SocieteGeneraleCreditCardFetcher societeGeneraleCreditCardFetcher =
                new SocieteGeneraleCreditCardFetcher(apiClient);

        SocieteGeneraleTransactionFetcher<CreditCardAccount> transactionFetcher =
                new SocieteGeneraleTransactionFetcher<>(apiClient);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                societeGeneraleCreditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }

    private TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new SocieteGeneraleTransferDestinationFetcher(apiClient));
    }

    private AgentConfiguration<SocieteGeneraleConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfiguration(SocieteGeneraleConfiguration.class);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        return Optional.of(
                new PaymentController(
                        new SocieteGeneralePaymentExecutor(
                                apiClient,
                                agentConfiguration.getRedirectUrl(),
                                sessionStorage,
                                supplementalInformationHelper,
                                strongAuthenticationState,
                                new FrCreatePaymentRequestValidator(localDateTimeSource))));
    }
}
