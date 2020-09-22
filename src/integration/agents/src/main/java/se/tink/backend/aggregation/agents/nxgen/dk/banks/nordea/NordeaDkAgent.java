package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

import com.google.inject.Inject;
import java.util.Collections;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.NordeaNemIdAuthenticatorV2;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.NordeaCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.investment.NordeaInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.NordeaDkLoansFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.NordeaAccountFetcher;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class NordeaDkAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor {

    private final NordeaDkApiClient nordeaClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;

    @Inject
    public NordeaDkAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        this.nordeaClient = constructNordeaClient();
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
        this.creditCardRefreshController = constructCreditCardRefreshController();
        this.investmentRefreshController = contructInvestmentRefreshController();
        this.loanRefreshController = constructLoanRefreshController();
    }

    private InvestmentRefreshController contructInvestmentRefreshController() {
        NordeaInvestmentFetcher fetcher = new NordeaInvestmentFetcher(nordeaClient);
        return new InvestmentRefreshController(metricRefreshController, updateController, fetcher);
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        NordeaCreditCardFetcher fetcher = new NordeaCreditCardFetcher(nordeaClient);
        return new CreditCardRefreshController(
                metricRefreshController, updateController, fetcher, fetcher);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        NordeaAccountFetcher fetcher = new NordeaAccountFetcher(nordeaClient);
        return new TransactionalAccountRefreshController(
                metricRefreshController, updateController, fetcher, fetcher);
    }

    private LoanRefreshController constructLoanRefreshController() {
        NordeaDkLoansFetcher fetcher = new NordeaDkLoansFetcher(nordeaClient);
        return new LoanRefreshController(metricRefreshController, updateController, fetcher);
    }

    private NordeaDkApiClient constructNordeaClient() {
        return new NordeaDkApiClient(sessionStorage, client, persistentStorage);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return new NordeaNemIdAuthenticatorV2(
                nordeaClient, sessionStorage, persistentStorage, supplementalRequester, catalog);
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
        return new FetchTransactionsResponse(Collections.emptyMap());
    }

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return loanRefreshController.fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return loanRefreshController.fetchLoanTransactions();
    }
}
