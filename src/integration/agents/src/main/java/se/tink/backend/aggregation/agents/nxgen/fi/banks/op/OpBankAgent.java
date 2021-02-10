package se.tink.backend.aggregation.agents.nxgen.fi.banks.op;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.OpAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.OpKeyCardAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.OpBankIdentityFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.OpBankInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.OpBankLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.creditcards.OpBankCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.transactionalaccounts.OpBankTransactionalAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.sessionhandler.OpBankSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@AgentCapabilities({SAVINGS_ACCOUNTS, CREDIT_CARDS, INVESTMENTS, IDENTITY_DATA, LOANS})
public final class OpBankAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    private final OpBankApiClient apiClient;
    private final OpBankPersistentStorage opBankPersistentStorage;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public OpBankAgent(final AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        apiClient = new OpBankApiClient(client);
        opBankPersistentStorage = new OpBankPersistentStorage(credentials, persistentStorage);

        investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new OpBankInvestmentFetcher(apiClient, credentials));

        loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new OpBankLoanFetcher(apiClient, credentials));

        creditCardRefreshController = constructCreditCardRefreshController();

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    @Override
    public Authenticator constructAuthenticator() {
        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new KeyCardAuthenticationController(
                        catalog,
                        supplementalInformationController,
                        new OpKeyCardAuthenticator(
                                apiClient, opBankPersistentStorage, credentials, sessionStorage),
                        OpBankConstants.KEYCARD_PIN_LENGTH),
                new OpAutoAuthenticator(apiClient, opBankPersistentStorage, credentials));
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
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
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
    public SessionHandler constructSessionHandler() {
        return new OpBankSessionHandler(apiClient);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return OpBankIdentityFetcher.fetchIdentity(sessionStorage);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new OpBankTransactionalAccountsFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new OpBankTransactionalAccountsFetcher(apiClient))));
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        OpBankCreditCardFetcher creditCardFetcher = new OpBankCreditCardFetcher(apiClient);
        TransactionFetcher<CreditCardAccount> creditCardTransactionFetcher =
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(creditCardFetcher));

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardFetcher,
                creditCardTransactionFetcher);
    }
}
