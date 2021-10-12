package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.Fetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.TimeoutRetryConfig;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.authenticator.SkandiaBankenAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.creditcard.SkandiaBankenCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.SkandiaBankenInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.transactionalaccount.SkandiaBankenAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.transactionalaccount.SkandiaBankenTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.transferdestination.SkandiaBankenTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.upcomingtransaction.SkandiaBankenUpcomingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.session.SkandiaBankenSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.GatewayTimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, INVESTMENTS})
public final class SkandiaBankenAgent extends NextGenerationAgent
        implements RefreshInvestmentAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshTransferDestinationExecutor {

    private final SkandiaBankenApiClient apiClient;
    private final InvestmentRefreshController investmentRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final TransferDestinationRefreshController transferDestinationRefreshController;

    @Inject
    public SkandiaBankenAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        apiClient =
                new SkandiaBankenApiClient(
                        client,
                        sessionStorage,
                        persistentStorage,
                        componentProvider.getContext().getLogMasker());
        configureHttpClient(client);
        investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new SkandiaBankenInvestmentFetcher(apiClient));
        creditCardRefreshController = constructCreditCardRefreshController();
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
        transferDestinationRefreshController = constructTransferDestinationRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(
                supplementalInformationController,
                new SkandiaBankenAuthenticator(apiClient, sessionStorage),
                persistentStorage,
                request);
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
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new SkandiaBankenAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(
                                new SkandiaBankenTransactionFetcher<>(apiClient),
                                Fetcher.START_PAGE),
                        new SkandiaBankenUpcomingTransactionFetcher(apiClient)));
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
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                new SkandiaBankenCreditCardFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(
                                new SkandiaBankenTransactionFetcher<>(apiClient),
                                Fetcher.START_PAGE)));
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return investmentRefreshController.fetchInvestmentAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return investmentRefreshController.fetchInvestmentTransactions();
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    private TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new SkandiaBankenTransferDestinationFetcher());
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SkandiaBankenSessionHandler(apiClient, persistentStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new TimeoutFilter());
        client.addFilter(
                new TimeoutRetryFilter(
                        TimeoutRetryConfig.NUM_TIMEOUT_RETRIES,
                        TimeoutRetryConfig.TIMEOUT_RETRY_SLEEP_MILLISECONDS));
        client.addFilter(new BankServiceInternalErrorFilter());
        client.addFilter(new GatewayTimeoutFilter());
    }
}
