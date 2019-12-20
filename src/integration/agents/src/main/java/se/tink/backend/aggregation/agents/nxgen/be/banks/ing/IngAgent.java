package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.ProgressiveAuthAgent;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.IngAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.IngCardReaderAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.controller.IngCardReaderAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.IngTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.IngTransferHelper;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.creditcard.IngCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.investment.IngInvestmentAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.IngTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.IngTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transferdestination.IngTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.filter.IngHttpFilter;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.session.IngSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.SubsequentGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.ProgressiveAuthController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationProgressiveController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.TimeoutRetryFilter;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class IngAgent extends SubsequentGenerationAgent<AutoAuthenticationProgressiveController>
        implements RefreshTransferDestinationExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshInvestmentAccountsExecutor,
                ProgressiveAuthAgent {
    private final IngApiClient apiClient;
    private final IngHelper ingHelper;
    private final IngTransferHelper ingTransferHelper;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private final AutoAuthenticationProgressiveController authenticator;

    public IngAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, true);
        configureHttpClient(client);
        this.apiClient =
                new IngApiClient(client, context.getAggregatorInfo().getAggregatorIdentifier());
        this.ingHelper = new IngHelper(sessionStorage);
        this.ingTransferHelper = new IngTransferHelper(catalog);

        this.transferDestinationRefreshController = constructTransferDestinationRefreshController();
        this.creditCardRefreshController = constructCreditCardRefreshController();
        this.investmentRefreshController = constructInvestmentRefreshController();

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();

        authenticator =
                new AutoAuthenticationProgressiveController(
                        request,
                        systemUpdater,
                        new IngCardReaderAuthenticationController(
                                new IngCardReaderAuthenticator(
                                        apiClient, persistentStorage, ingHelper),
                                supplementalInformationFormer),
                        new IngAutoAuthenticator(apiClient, persistentStorage, ingHelper));
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.setUserAgent(Headers.USER_AGENT);
        client.setFollowRedirects(false);
        client.addFilter(new IngHttpFilter());
        client.addFilter(
                new TimeoutRetryFilter(
                        IngConstants.HttpClient.MAX_RETRIES,
                        IngConstants.HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(new TimeoutFilter());
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
        IngTransactionFetcher transactionFetcher =
                new IngTransactionFetcher(credentials, apiClient, ingHelper);

        TransactionPagePaginationController<TransactionalAccount>
                transactionPagePaginationController =
                        new TransactionPagePaginationController<>(
                                transactionFetcher, IngConstants.Fetcher.START_PAGE);

        TransactionFetcherController<TransactionalAccount> transactionFetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        transactionPagePaginationController,
                        transactionFetcher);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new IngTransactionalAccountFetcher(apiClient, ingHelper),
                transactionFetcherController);
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
        IngCreditCardFetcher creditCardFetcher = new IngCreditCardFetcher(ingHelper);

        return new CreditCardRefreshController(
                metricRefreshController, updateController, creditCardFetcher, creditCardFetcher);
    }

    private InvestmentRefreshController constructInvestmentRefreshController() {
        final IngInvestmentAccountFetcher investmentAccountFetcher =
                new IngInvestmentAccountFetcher(apiClient, ingHelper);
        return new InvestmentRefreshController(
                metricRefreshController,
                updateController,
                investmentAccountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(investmentAccountFetcher, 1)));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    private TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new IngTransferDestinationFetcher(apiClient, ingHelper));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new IngSessionHandler(apiClient, ingHelper);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        BankTransferExecutor bankTransferExecutor =
                new IngTransferExecutor(apiClient, persistentStorage, ingHelper, ingTransferHelper);

        return Optional.of(new TransferController(null, bankTransferExecutor, null, null));
    }

    @Override
    public AutoAuthenticationProgressiveController getAuthenticator() {
        return authenticator;
    }

    @Override
    public SteppableAuthenticationResponse login(final SteppableAuthenticationRequest request)
            throws Exception {
        return ProgressiveAuthController.of(authenticator, credentials).login(request);
    }

    @Override
    public boolean login() throws Exception {
        throw new AssertionError(); // ProgressiveAuthAgent::login should always be used
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return investmentRefreshController.fetchInvestmentAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return investmentRefreshController.fetchInvestmentTransactions();
    }
}
