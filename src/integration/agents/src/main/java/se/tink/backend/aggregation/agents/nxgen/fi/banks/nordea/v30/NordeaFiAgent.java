package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30;

import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.authenticator.NordeaCodesAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.creditcard.NordeaCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.investment.NordeaInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.loan.NordeaLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.transactionalaccount.NordeaTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.transactionalaccount.NordeaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.session.NordeaSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index.TransactionIndexPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NordeaFiAgent extends NextGenerationAgent
        implements RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    private final NordeaFiApiClient apiClient;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public NordeaFiAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.apiClient =
                new NordeaFiApiClient(
                        client, sessionStorage, credentials.getField(Field.Key.USERNAME));

        this.investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new NordeaInvestmentFetcher(apiClient));

        this.loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new NordeaLoanFetcher(apiClient));

        this.creditCardRefreshController = constructCreditCardRefreshController();

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new ThirdPartyAppAuthenticationController<>(
                new NordeaCodesAuthenticator(apiClient, sessionStorage),
                supplementalInformationHelper);
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
        NordeaTransactionalAccountFetcher accountFetcher =
                new NordeaTransactionalAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionIndexPaginationController<>(
                                new NordeaTransactionFetcher(apiClient))));
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
        NordeaCreditCardFetcher creditCardFetcher = new NordeaCreditCardFetcher(apiClient);
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionIndexPaginationController<>(creditCardFetcher)));
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
    protected SessionHandler constructSessionHandler() {
        return new NordeaSessionHandler(apiClient);
    }
}
