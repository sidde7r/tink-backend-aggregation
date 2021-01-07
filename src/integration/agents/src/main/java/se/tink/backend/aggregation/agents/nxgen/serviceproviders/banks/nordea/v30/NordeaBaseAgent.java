package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.TransactionFetching;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.NordeaBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.NordeaPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc.BankIdAutostartResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.NordeaBankTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.NordeaExecutorHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.NordeaPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.creditcard.NordeaCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.creditcard.NordeaCreditCardTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.identitydata.NordeaSeIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.investment.NordeaInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.NordeaLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transactionalaccount.NordeaTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transactionalaccount.NordeaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transactionalaccount.NordeaUpcomingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transfer.NordeaTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.filters.NordeaInternalServerErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.filters.NordeaServiceUnavailableFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.session.NordeaSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;

public abstract class NordeaBaseAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshTransferDestinationExecutor,
                RefreshIdentityDataExecutor {
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final NordeaBaseApiClient apiClient;
    private final NordeaConfiguration nordeaConfiguration;
    private final LocalDateTimeSource localDateTimeSource;
    private final String organisationNumber;
    private final RandomValueGenerator randomValueGenerator;

    public NordeaBaseAgent(
            AgentComponentProvider componentProvider, NordeaConfiguration nordeaConfiguration) {
        super(componentProvider);
        this.nordeaConfiguration = nordeaConfiguration;
        apiClient = new NordeaBaseApiClient(client, sessionStorage, nordeaConfiguration);
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
        randomValueGenerator = componentProvider.getRandomValueGenerator();
        this.organisationNumber =
                Optional.ofNullable(
                                componentProvider
                                        .getCredentialsRequest()
                                        .getCredentials()
                                        .getField(Key.CORPORATE_ID))
                        .map(s -> s.replace("-", "").trim())
                        .orElse("");

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
        creditCardRefreshController = constructCreditCardRefreshController();
        investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new NordeaInvestmentFetcher(apiClient));
        loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new NordeaLoanFetcher(apiClient));

        transferDestinationRefreshController = constructTransferDestinationRefreshController();

        configureHttpClient(client);
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
        final NordeaTransactionFetcher transactionFetcher =
                new NordeaTransactionFetcher(apiClient, nordeaConfiguration);
        final NordeaUpcomingTransactionFetcher upcomingTransactionFetcher =
                new NordeaUpcomingTransactionFetcher(apiClient, nordeaConfiguration);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new NordeaTransactionalAccountFetcher(apiClient, nordeaConfiguration),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(transactionFetcher)
                                .setConsecutiveEmptyPagesLimit(
                                        TransactionFetching.MAX_CONSECUTIVE_EMPTY_PAGES)
                                .setAmountAndUnitToFetch(
                                        TransactionFetching.MONTHS_TO_PAGINATE, ChronoUnit.MONTHS)
                                .setLocalDateTimeSource(localDateTimeSource)
                                .build(),
                        upcomingTransactionFetcher));
    }

    private void configureHttpClient(final TinkHttpClient client) {
        client.addFilter(new NordeaServiceUnavailableFilter());
        client.addFilter(new NordeaInternalServerErrorFilter());
        client.addFilter(new TimeoutFilter());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        BankIdAuthenticationController<BankIdAutostartResponse> bankIdAuthenticationController =
                new BankIdAuthenticationController<>(
                        context,
                        new NordeaBankIdAuthenticator(
                                apiClient, sessionStorage, nordeaConfiguration, organisationNumber),
                        persistentStorage,
                        credentials);

        NordeaPasswordAuthenticator passwordAuthenticator =
                new NordeaPasswordAuthenticator(
                        request,
                        context,
                        apiClient,
                        persistentStorage,
                        sessionStorage,
                        bankIdAuthenticationController,
                        nordeaConfiguration);

        AutoAuthenticationController passwordAuthenticationController =
                new AutoAuthenticationController(
                        request, systemUpdater, passwordAuthenticator, passwordAuthenticator);

        return new TypedAuthenticationController(
                bankIdAuthenticationController, passwordAuthenticationController);
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
                new NordeaCreditCardFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(
                                new NordeaCreditCardTransactionsFetcher(apiClient),
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
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return loanRefreshController.fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return loanRefreshController.fetchLoanTransactions();
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    private TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new NordeaTransferDestinationFetcher(apiClient));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new NordeaSessionHandler(apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        NordeaExecutorHelper executorHelper =
                new NordeaExecutorHelper(
                        context, catalog, apiClient, nordeaConfiguration, randomValueGenerator);

        return Optional.of(
                new TransferController(
                        new NordeaPaymentExecutor(apiClient, executorHelper),
                        new NordeaBankTransferExecutor(apiClient, catalog, executorHelper)));
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(
                new NordeaSeIdentityDataFetcher(apiClient, nordeaConfiguration)
                        .fetchIdentityData());
    }
}
