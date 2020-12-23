package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.N26AuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.N26TransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.N26TransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.payment.N26PaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.storage.N26Storage;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class N26Agent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshTransferDestinationExecutor {

    private final N26Storage storage;
    private final N26ApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final RandomValueGenerator randomValueGenerator;

    @Inject
    public N26Agent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.storage = new N26Storage(persistentStorage);
        client.setTimeout(N26Constants.Timeout.TIMEOUT_IN_MILLISECONDS);
        client.addFilter(
                new TimeoutRetryFilter(
                        N26Constants.Timeout.NUM_TIMEOUT_RETRIES,
                        N26Constants.Timeout.TIMEOUT_RETRY_SLEEP_MILLISECONDS));
        this.apiClient = new N26ApiClient(client, getAgentConfig(), storage);
        this.transactionalAccountRefreshController = initTransactionalAccountFetcher();
        this.transferDestinationRefreshController = constructTransferDestinationRefreshController();
        this.randomValueGenerator = componentProvider.getRandomValueGenerator();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final N26AuthenticationController controller =
                new N26AuthenticationController(
                        apiClient,
                        getAgentConfig(),
                        supplementalInformationHelper,
                        strongAuthenticationState,
                        storage,
                        credentials,
                        randomValueGenerator);

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

    private AgentConfiguration<N26Configuration> getAgentConfig() {
        return getAgentConfigurationController().getAgentConfiguration(N26Configuration.class);
    }

    private TransactionalAccountRefreshController initTransactionalAccountFetcher() {
        N26TransactionalAccountFetcher fetcher = new N26TransactionalAccountFetcher(apiClient);
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                fetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(fetcher)));
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        final AgentConfiguration<N26Configuration> agentConfiguration = getAgentConfig();

        N26PaymentExecutor paymentExecutor =
                new N26PaymentExecutor(
                        apiClient,
                        agentConfiguration,
                        strongAuthenticationState,
                        supplementalInformationHelper);

        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    private TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new N26TransferDestinationFetcher(apiClient));
    }
}
