package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar;

import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchEInvoicesResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshEInvoiceExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.Fetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.authenticator.LansforsakringarBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.authenticator.rpc.BankIdInitResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.creditcard.LansforsakringarCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.einvoice.LansforsakringarEinvoiceFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.LansforsakringarInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.loan.LansforsakringarLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.LansforsakringarTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.LansforsakringarTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.LansforsakringarUpcomingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transfer.LansforsakringarTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.session.LansforsakringarSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.identitydata.countries.SeIdentityData;

public class LansforsakringarAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshTransferDestinationExecutor,
                RefreshEInvoiceExecutor,
                RefreshCreditCardAccountsExecutor {

    private final LansforsakringarApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final EInvoiceRefreshController einvoiceRefreshController;

    @Inject
    public LansforsakringarAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        apiClient =
                new LansforsakringarApiClient(client, sessionStorage, catalog, persistentStorage);
        persistentStorage.computeIfAbsent(
                HeaderKeys.DEVICE_ID, k -> UUID.randomUUID().toString().toUpperCase());
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
        creditCardRefreshController = constructCreditCardRefreshController();
        investmentRefreshController = constructInvestmentRefreshController();
        loanRefreshController = constructLoanRefreshController();
        transferDestinationRefreshController = constructTransferDestinationRefreshController();
        einvoiceRefreshController = constructEinvoiceRefreshController();
    }

    private EInvoiceRefreshController constructEinvoiceRefreshController() {
        return new EInvoiceRefreshController(
                metricRefreshController, new LansforsakringarEinvoiceFetcher(apiClient));
    }

    private TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new LansforsakringarTransferDestinationFetcher(apiClient));
    }

    private LoanRefreshController constructLoanRefreshController() {
        return new LoanRefreshController(
                metricRefreshController,
                updateController,
                new LansforsakringarLoanFetcher(apiClient));
    }

    private InvestmentRefreshController constructInvestmentRefreshController() {
        return new InvestmentRefreshController(
                metricRefreshController,
                updateController,
                new LansforsakringarInvestmentFetcher(apiClient));
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        final LansforsakringarCreditCardFetcher lansforsakringarCreditCardFetcher =
                new LansforsakringarCreditCardFetcher(apiClient);
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                lansforsakringarCreditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(
                                lansforsakringarCreditCardFetcher,
                                Fetcher.CREDIT_CARD_START_PAGE)));
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new LansforsakringarTransactionalAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(
                                new LansforsakringarTransactionFetcher(apiClient),
                                Fetcher.START_PAGE),
                        new LansforsakringarUpcomingTransactionFetcher(apiClient)));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        BankIdAuthenticationController<BankIdInitResponse>
                bankIdResponseBankIdAuthenticationController =
                        new BankIdAuthenticationController(
                                supplementalInformationController,
                                new LansforsakringarBankIdAuthenticator(apiClient, sessionStorage),
                                persistentStorage,
                                credentials);
        return new TypedAuthenticationController(bankIdResponseBankIdAuthenticationController);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new LansforsakringarSessionHandler(apiClient);
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
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(
                SeIdentityData.of(
                        sessionStorage.get(StorageKeys.CUSTOMER_NAME),
                        sessionStorage.get(StorageKeys.SSN)));
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return investmentRefreshController.fetchInvestmentAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return new FetchTransactionsResponse(Collections.emptyMap());
    }

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return loanRefreshController.fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return new FetchTransactionsResponse(Collections.emptyMap());
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    @Override
    public FetchEInvoicesResponse fetchEInvoices() {
        return new FetchEInvoicesResponse(einvoiceRefreshController.refreshEInvoices());
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    /* Handover to payments team
    @Override
    protected Optional<TransferController> constructTransferController() {
        LansforsakringarExecutorHelper lansforsakringarExecutorHelper =
                new LansforsakringarExecutorHelper(apiClient, context, catalog);
        return Optional.of(
                new TransferController(
                        null,
                        new LansforsakringarBankTransferExecutor(
                                apiClient, lansforsakringarExecutorHelper),
                        null,
                        null));
    }

     */
}
