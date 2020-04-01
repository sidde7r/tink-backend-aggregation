package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider;

import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchEInvoicesResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshEInvoiceExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.FeatureFlag;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.SwedbankDefaultBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.SwedbankTransferHelper;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.einvoice.SwedbankDefaultApproveEInvoiceExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.payment.SwedbankDefaultPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.transfer.SwedbankDefaultBankTransferExecutorNxgen;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.updatepayment.SwedbankDefaultUpdatePaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.creditcard.SwedbankDefaultCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.einvoice.SwedbankDefaultEinvoiceFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.investment.SwedbankDefaultInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.loan.SwedbankDefaultLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transactional.SwedbankDefaultTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.SwedbankDefaultTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.filters.SwedbankBaseHttpFilter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.interfaces.SwedbankApiClientProvider;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class SwedbankAbstractAgentPaymentsRevamp extends NextGenerationAgent
        implements RefreshEInvoiceExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshTransferDestinationExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {
    protected final SwedbankConfiguration configuration;
    protected final SwedbankDefaultApiClient apiClient;
    private EInvoiceRefreshController eInvoiceRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final boolean isBankId;
    private final SwedbankStorage swedbankStorage;
    private final AgentsServiceConfiguration agentsServiceConfiguration;

    public SwedbankAbstractAgentPaymentsRevamp(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration,
            SwedbankConfiguration configuration) {
        this(
                request,
                context,
                agentsServiceConfiguration,
                configuration,
                new SwedbankDefaultApiClientProvider());
    }

    protected SwedbankAbstractAgentPaymentsRevamp(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration,
            SwedbankConfiguration configuration,
            SwedbankApiClientProvider apiClientProvider) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());
        this.agentsServiceConfiguration = agentsServiceConfiguration;
        configureHttpClient(client);
        swedbankStorage = new SwedbankStorage();

        this.isBankId =
                request.getProvider().getCredentialsType().equals(CredentialsTypes.MOBILE_BANKID);
        this.configuration = configuration;
        this.apiClient =
                apiClientProvider.getApiAgent(client, configuration, credentials, swedbankStorage);
        eInvoiceRefreshController = null;

        SwedbankDefaultInvestmentFetcher investmentFetcher =
                new SwedbankDefaultInvestmentFetcher(
                        apiClient, request.getProvider().getCurrency());

        investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController, updateController, investmentFetcher);

        loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new SwedbankDefaultLoanFetcher(apiClient));

        transferDestinationRefreshController = constructTransferDestinationRefreshController();

        creditCardRefreshController = constructCreditCardRefreshController();

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new SwedbankBaseHttpFilter());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(
                supplementalRequester,
                new SwedbankDefaultBankIdAuthenticator(apiClient, sessionStorage),
                persistentStorage,
                credentials);
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
        SwedbankDefaultTransactionalAccountFetcher transactionalFetcher =
                new SwedbankDefaultTransactionalAccountFetcher(apiClient, persistentStorage);

        TransactionFetcherController<TransactionalAccount> transactionFetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionalFetcher),
                        transactionalFetcher);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                transactionalFetcher,
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
        SwedbankDefaultCreditCardFetcher creditCardFetcher =
                new SwedbankDefaultCreditCardFetcher(
                        apiClient, request.getProvider().getCurrency());

        TransactionFetcherController<CreditCardAccount> transactionFetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(creditCardFetcher));

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardFetcher,
                transactionFetcherController);
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
                metricRefreshController,
                new SwedbankDefaultTransferDestinationFetcher(apiClient, sessionStorage));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SwedbankDefaultSessionHandler(
                apiClient,
                agentsServiceConfiguration.isFeatureEnabled(FeatureFlag.CHECK_KEEP_ALIVE));
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        SwedbankTransferHelper transferHelper =
                new SwedbankTransferHelper(
                        context, catalog, supplementalInformationHelper, apiClient, isBankId);
        SwedbankDefaultBankTransferExecutorNxgen transferExecutor =
                new SwedbankDefaultBankTransferExecutorNxgen(
                        catalog, apiClient, transferHelper, swedbankStorage);
        SwedbankDefaultPaymentExecutor paymentExecutor =
                new SwedbankDefaultPaymentExecutor(
                        catalog, apiClient, transferHelper, swedbankStorage);
        SwedbankDefaultApproveEInvoiceExecutor approveEInvoiceExecutor =
                new SwedbankDefaultApproveEInvoiceExecutor(apiClient, transferHelper);
        SwedbankDefaultUpdatePaymentExecutor updatePaymentExecutor =
                new SwedbankDefaultUpdatePaymentExecutor(apiClient, transferHelper);
        return Optional.of(
                new TransferController(
                        paymentExecutor,
                        transferExecutor,
                        approveEInvoiceExecutor,
                        updatePaymentExecutor));
    }

    @Override
    public FetchEInvoicesResponse fetchEInvoices() {
        eInvoiceRefreshController =
                Optional.ofNullable(eInvoiceRefreshController)
                        .orElseGet(
                                () ->
                                        new EInvoiceRefreshController(
                                                metricRefreshController,
                                                new SwedbankDefaultEinvoiceFetcher(apiClient)));
        return new FetchEInvoicesResponse(eInvoiceRefreshController.refreshEInvoices());
    }
}
