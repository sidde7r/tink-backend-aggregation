package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.IngAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.IngCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.IngIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.IngInvestmentAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.IngLoanAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.IngTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.IngTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.session.IngSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class IngAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor {

    private final IngApiClient ingApiClient;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;

    public IngAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {

        super(request, context, signatureKeyPair);

        this.ingApiClient = new IngApiClient(this.client);

        IngInvestmentAccountFetcher accountFetcher = new IngInvestmentAccountFetcher(ingApiClient);
        this.investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController, updateController, accountFetcher);

        this.loanRefreshController = constructLoanRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new IngAuthenticator(this.ingApiClient);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {

        IngTransactionalAccountFetcher accountFetcher =
                new IngTransactionalAccountFetcher(ingApiClient, sessionStorage);
        IngTransactionFetcher transactionFetcher = new IngTransactionFetcher(ingApiClient);

        TransactionMonthPaginationController<TransactionalAccount> paginationController =
                new TransactionMonthPaginationController<>(
                        transactionFetcher, IngConstants.ZONE_ID);

        TransactionFetcherController<TransactionalAccount> fetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper, paginationController);

        TransactionalAccountRefreshController refreshController =
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        fetcherController);

        return Optional.of(refreshController);
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {

        IngCreditCardAccountFetcher accountFetcher = new IngCreditCardAccountFetcher(ingApiClient);
        IngTransactionFetcher transactionFetcher = new IngTransactionFetcher(ingApiClient);

        TransactionMonthPaginationController<CreditCardAccount> paginationController =
                new TransactionMonthPaginationController<>(
                        transactionFetcher, IngConstants.ZONE_ID);

        TransactionFetcherController<CreditCardAccount> fetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper, paginationController);

        CreditCardRefreshController refreshController =
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        fetcherController);

        return Optional.of(refreshController);
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

    private LoanRefreshController constructLoanRefreshController() {
        IngLoanAccountFetcher accountFetcher = new IngLoanAccountFetcher(ingApiClient);
        IngTransactionFetcher transactionFetcher = new IngTransactionFetcher(ingApiClient);

        TransactionMonthPaginationController<LoanAccount> paginationController =
                new TransactionMonthPaginationController<>(
                        transactionFetcher, IngConstants.ZONE_ID);

        TransactionFetcherController<LoanAccount> fetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper, paginationController);

        return new LoanRefreshController(
                metricRefreshController, updateController, accountFetcher, fetcherController);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new IngSessionHandler(ingApiClient);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        final IdentityDataFetcher fetcher = new IngIdentityDataFetcher(ingApiClient);
        return new FetchIdentityDataResponse(fetcher.fetchIdentityData());
    }
}
