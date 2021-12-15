package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
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
import se.tink.backend.aggregation.agents.agentcapabilities.Capability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.LansforsakringarDecoupledAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.configuration.LansforsakringarConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.LansforsakringarPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.creditcard.LansforsakringarCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.creditcard.LansforsakringarCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transactionalaccount.LansforsakringarTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transactionalaccount.LansforsakringarTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transfersdestinations.LansforsakringarTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.filter.ConsentErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.filter.LansforsakringarRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.filter.ServerFaultFilter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.filter.ServiceBlockedFilter;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

@AgentCapabilities({
    Capability.CHECKING_ACCOUNTS,
    Capability.SAVINGS_ACCOUNTS,
    Capability.CREDIT_CARDS,
    Capability.TRANSFERS
})
@AgentPisCapability(
        capabilities = {
            PisCapability.PIS_SE_BANK_TRANSFERS,
            PisCapability.PIS_SE_BG,
            PisCapability.PIS_SE_PG,
            PisCapability.PIS_FUTURE_DATE
        },
        markets = {"SE"})
public final class LansforsakringarAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshTransferDestinationExecutor {

    private final LansforsakringarApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final LocalDateTimeSource localDateTimeSource;
    private final LansforsakringarStorageHelper storageHelper;
    private final TransferDestinationRefreshController transferDestinationRefreshController;

    @Inject
    public LansforsakringarAgent(
            AgentComponentProvider componentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(componentProvider);
        configureHttpClient(client);
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
        this.storageHelper = new LansforsakringarStorageHelper(persistentStorage);

        apiClient =
                new LansforsakringarApiClient(
                        client, credentials, storageHelper, componentProvider.getUser());

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        creditCardRefreshController = getCreditCardRefreshController();
        setAgentConfiguration();

        this.client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());

        transferDestinationRefreshController = constructTransferDestinationController(apiClient);
    }

    private void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new ConsentErrorFilter());
        client.addFilter(new ServiceBlockedFilter(systemUpdater, credentials));
        client.addFilter(
                new LansforsakringarRetryFilter(
                        LansforsakringarConstants.MAX_NUM_RETRIES,
                        LansforsakringarConstants.RETRY_SLEEP_MILLIS_SECONDS));
        client.addFilter(new ServerFaultFilter());
        client.setTimeout(LansforsakringarConstants.TIME_OUT_MILLIS);
    }

    private void setAgentConfiguration() {
        final AgentConfiguration<LansforsakringarConfiguration> agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(LansforsakringarConfiguration.class);
        apiClient.setConfiguration(agentConfiguration);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        BankIdAuthenticationController<String> bankIdAuthenticationController =
                new BankIdAuthenticationController<>(
                        supplementalInformationController,
                        new LansforsakringarDecoupledAuthenticator(
                                apiClient, storageHelper, credentials),
                        persistentStorage,
                        request);

        return new AutoAuthenticationController(
                request, context, bankIdAuthenticationController, bankIdAuthenticationController);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        LansforsakringarPaymentExecutor lansforsakringarPaymentExecutor =
                new LansforsakringarPaymentExecutor(
                        apiClient, supplementalInformationHelper, strongAuthenticationState);

        return Optional.of(
                new PaymentController(
                        lansforsakringarPaymentExecutor, lansforsakringarPaymentExecutor));
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

    private TransferDestinationRefreshController constructTransferDestinationController(
            LansforsakringarApiClient apiClient) {
        return new TransferDestinationRefreshController(
                metricRefreshController, new LansforsakringarTransferDestinationFetcher(apiClient));
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final LansforsakringarTransactionalAccountFetcher accountFetcher =
                new LansforsakringarTransactionalAccountFetcher(apiClient);

        final LansforsakringarTransactionFetcher<TransactionalAccount> transactionFetcher =
                new LansforsakringarTransactionFetcher<>(apiClient, localDateTimeSource);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }

    private CreditCardRefreshController getCreditCardRefreshController() {
        LansforsakringarCreditCardFetcher creditCardFetcher =
                new LansforsakringarCreditCardFetcher(apiClient);

        final LansforsakringarCreditCardTransactionFetcher transactionFetcher =
                new LansforsakringarCreditCardTransactionFetcher(apiClient, localDateTimeSource);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new LansforsakringarSessionHandler(apiClient, storageHelper);
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
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }
}
