package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.SwedbankAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.SwedbankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.authenticator.SwedbankPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.SwedbankPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.SwedbankTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.SwedbankTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transferdestinations.SwedbankTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.filter.SwedbankConsentLimitFilter;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;

/** This agent is not ready for production. Its for test and documentation of the flow. */
@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS})
public final class SwedbankAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshTransferDestinationExecutor {

    private final SwedbankApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final TransferDestinationRefreshController transferDestinationRefreshController;

    @Inject
    public SwedbankAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);
        client.addFilter(new SwedbankConsentLimitFilter());
        client.addFilter(new BankServiceInternalErrorFilter());
        apiClient =
                new SwedbankApiClient(
                        client,
                        persistentStorage,
                        getAgentConfiguration(),
                        qsealcSigner,
                        componentProvider.getCredentialsRequest());

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        transferDestinationRefreshController = constructTransferDestinationController();
    }

    private TransferDestinationRefreshController constructTransferDestinationController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new SwedbankTransferDestinationFetcher());
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration agentsServiceConfiguration) {
        super.setConfiguration(agentsServiceConfiguration);
        client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
    }

    private AgentConfiguration<SwedbankConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController().getAgentConfiguration(SwedbankConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {

        SwedbankAuthenticator authenticator =
                new SwedbankAuthenticator(apiClient, persistentStorage);

        SwedbankAuthenticationController swedbankAuthenticationController =
                new SwedbankAuthenticationController(
                        persistentStorage, supplementalRequester, authenticator, credentials);

        return new AutoAuthenticationController(
                request,
                context,
                new BankIdAuthenticationController<>(
                        supplementalRequester,
                        swedbankAuthenticationController,
                        persistentStorage,
                        credentials),
                swedbankAuthenticationController);
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
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new SwedbankTransactionalAccountFetcher(apiClient),
                new SwedbankTransactionFetcher(apiClient, supplementalInformationHelper));
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        SwedbankPaymentAuthenticator paymentAuthenticator =
                new SwedbankPaymentAuthenticator(supplementalInformationHelper);
        SwedbankPaymentExecutor swedbankPaymentExecutor =
                new SwedbankPaymentExecutor(
                        apiClient, paymentAuthenticator, strongAuthenticationState);

        return Optional.of(new PaymentController(swedbankPaymentExecutor, swedbankPaymentExecutor));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }
}
