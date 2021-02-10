package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.MORTGAGE_AGGREGATION;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.PAYMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
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
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants.TimeoutFilter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.IcaBankenBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.IcaBankenBankTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.IcaBankenExecutorHelper;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.IcaBankenPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.IcaBankenCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.IcaBankenTransactionalAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.IcaBankenEInvoiceFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.identitydata.IcaBankenIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.IcaBankenInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.IcaBankenLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.IcaBankenTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.filter.IcaBankenFilter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.storage.IcaBankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.storage.IcabankenPersistentStorage;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
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
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    LOANS,
    PAYMENTS,
    CREDIT_CARDS,
    SAVINGS_ACCOUNTS,
    IDENTITY_DATA,
    TRANSFERS,
    INVESTMENTS,
    MORTGAGE_AGGREGATION
})
public final class IcaBankenAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshEInvoiceExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshTransferDestinationExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    private final IcaBankenApiClient apiClient;
    private final IcaBankenSessionStorage icaBankenSessionStorage;
    private final IcabankenPersistentStorage icaBankenPersistentStorage;
    private EInvoiceRefreshController eInvoiceRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public IcaBankenAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        configureHttpClient(client);
        this.icaBankenSessionStorage = new IcaBankenSessionStorage(sessionStorage);
        this.icaBankenPersistentStorage =
                new IcabankenPersistentStorage(
                        persistentStorage, componentProvider.getRandomValueGenerator());
        this.apiClient =
                new IcaBankenApiClient(client, icaBankenSessionStorage, icaBankenPersistentStorage);
        this.eInvoiceRefreshController = null;

        this.investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new IcaBankenInvestmentFetcher(apiClient, icaBankenSessionStorage));

        this.loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new IcaBankenLoanFetcher(apiClient, icaBankenSessionStorage));

        this.transferDestinationRefreshController = constructTransferDestinationRefreshController();

        this.creditCardRefreshController = constructCreditCardRefreshController();

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new IcaBankenFilter());
        client.addFilter(
                new TimeoutRetryFilter(
                        TimeoutFilter.NUM_TIMEOUT_RETRIES,
                        TimeoutFilter.TIMEOUT_RETRY_SLEEP_MILLISECONDS));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(
                supplementalInformationController,
                new IcaBankenBankIdAuthenticator(apiClient, icaBankenSessionStorage),
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
        IcaBankenTransactionalAccountsFetcher transactionalAccountFetcher =
                new IcaBankenTransactionalAccountsFetcher(apiClient, icaBankenSessionStorage);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                transactionalAccountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionalAccountFetcher),
                        transactionalAccountFetcher));
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
        IcaBankenCreditCardFetcher creditCardFetcher =
                new IcaBankenCreditCardFetcher(apiClient, icaBankenSessionStorage);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(creditCardFetcher)));
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
                new IcaBankenTransferDestinationFetcher(apiClient, icaBankenSessionStorage));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new IcaBankenSessionHandler(apiClient, icaBankenSessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        IcaBankenExecutorHelper executorHelper =
                new IcaBankenExecutorHelper(
                        apiClient, context, catalog, supplementalInformationController);

        return Optional.of(
                new TransferController(
                        new IcaBankenPaymentExecutor(apiClient, executorHelper),
                        new IcaBankenBankTransferExecutor(apiClient, executorHelper, catalog)));
    }

    @Override
    public FetchEInvoicesResponse fetchEInvoices() {
        final IcaBankenEInvoiceFetcher eInvoiceFetcher =
                new IcaBankenEInvoiceFetcher(apiClient, icaBankenSessionStorage, catalog);
        eInvoiceRefreshController =
                Optional.ofNullable(eInvoiceRefreshController)
                        .orElseGet(
                                () ->
                                        new EInvoiceRefreshController(
                                                metricRefreshController, eInvoiceFetcher));
        return new FetchEInvoicesResponse(eInvoiceRefreshController.refreshEInvoices());
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        IcaBankenIdentityDataFetcher identityDataFetcher =
                new IcaBankenIdentityDataFetcher(apiClient);
        return identityDataFetcher.getIdentityDataResponse();
    }
}
