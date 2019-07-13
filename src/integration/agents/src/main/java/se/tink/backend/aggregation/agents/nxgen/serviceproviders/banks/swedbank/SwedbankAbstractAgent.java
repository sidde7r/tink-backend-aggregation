package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank;

import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchEInvoicesResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshEInvoiceExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants.TimeoutFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.authenticator.SwedbankDefaultBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.SwedbankTransferHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.einvoice.SwedbankDefaultApproveEInvoiceExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.payment.SwedbankDefaultPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.transfer.SwedbankDefaultBankTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.updatepayment.SwedbankDefaultUpdatePaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.creditcard.SwedbankDefaultCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.einvoice.SwedbankDefaultEinvoiceFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.identitydata.SwedbankIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.SwedbankDefaultInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.loan.SwedbankDefaultLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transactional.SwedbankDefaultTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.SwedbankDefaultTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.filters.SwedbankBaseHttpFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.interfaces.SwedbankApiClientProvider;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
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
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.TimeoutRetryFilter;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class SwedbankAbstractAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshEInvoiceExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshTransferDestinationExecutor,
                RefreshCreditCardAccountsExecutor {

    protected final SwedbankConfiguration configuration;
    protected final SwedbankDefaultApiClient apiClient;
    private EInvoiceRefreshController eInvoiceRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;

    public SwedbankAbstractAgent(
            CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair,
            SwedbankConfiguration configuration) {
        this(
                request,
                context,
                signatureKeyPair,
                configuration,
                new SwedbankDefaultApiClientProvider());
    }

    protected SwedbankAbstractAgent(
            CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair,
            SwedbankConfiguration configuration,
            SwedbankApiClientProvider apiClientProvider) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);
        this.configuration = configuration;
        this.apiClient =
                apiClientProvider.getApiAgent(client, configuration, credentials, sessionStorage);
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
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new SwedbankBaseHttpFilter());
        client.addFilter(
                new TimeoutRetryFilter(
                        TimeoutFilter.NUM_TIMEOUT_RETRIES,
                        TimeoutFilter.TIMEOUT_RETRY_SLEEP_MILLISECONDS));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(
                supplementalRequester,
                new SwedbankDefaultBankIdAuthenticator(apiClient),
                persistentStorage);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        SwedbankDefaultTransactionalAccountFetcher transactionalFetcher =
                new SwedbankDefaultTransactionalAccountFetcher(apiClient, persistentStorage);

        TransactionFetcherController<TransactionalAccount> transactionFetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionalFetcher),
                        transactionalFetcher);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        transactionalFetcher,
                        transactionFetcherController));
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
                metricRefreshController, new SwedbankDefaultTransferDestinationFetcher(apiClient));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SwedbankDefaultSessionHandler(apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        SwedbankTransferHelper transferHelper =
                new SwedbankTransferHelper(
                        context, catalog, supplementalInformationHelper, apiClient);
        SwedbankDefaultBankTransferExecutor transferExecutor =
                new SwedbankDefaultBankTransferExecutor(catalog, apiClient, transferHelper);
        SwedbankDefaultPaymentExecutor paymentExecutor =
                new SwedbankDefaultPaymentExecutor(catalog, apiClient, transferHelper);
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

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        final SwedbankIdentityDataFetcher identityDataFetcher =
                new SwedbankIdentityDataFetcher(apiClient);

        return identityDataFetcher.getIdentityDataResponse();
    }
}
