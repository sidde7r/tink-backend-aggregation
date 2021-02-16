package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.MORTGAGE_AGGREGATION;

import com.google.inject.Inject;
import java.util.Collections;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.NordeaNemIdAuthenticatorV2;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.NordeaCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.NordeaCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.identitydata.NordeaDKIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.investment.NordeaInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.NordeaDkLoansFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.NordeaAccountFetcher;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    CREDIT_CARDS,
    INVESTMENTS,
    LOANS,
    MORTGAGE_AGGREGATION,
    IDENTITY_DATA
})
public final class NordeaDkAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshIdentityDataExecutor {

    private final NordeaDkApiClient nordeaClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final StatusUpdater statusUpdater;

    @Inject
    public NordeaDkAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        this.nordeaClient = constructNordeaClient();
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
        this.creditCardRefreshController = constructCreditCardRefreshController();
        this.investmentRefreshController = contructInvestmentRefreshController();
        this.loanRefreshController = constructLoanRefreshController();
        statusUpdater = agentComponentProvider.getContext();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        NordeaNemIdAuthenticatorV2 nordeaNemIdAuthenticatorV2 =
                new NordeaNemIdAuthenticatorV2(
                        nordeaClient,
                        sessionStorage,
                        persistentStorage,
                        catalog,
                        statusUpdater,
                        supplementalInformationController,
                        metricContext);
        return new AutoAuthenticationController(
                request, systemUpdater, nordeaNemIdAuthenticatorV2, nordeaNemIdAuthenticatorV2);
    }

    private InvestmentRefreshController contructInvestmentRefreshController() {
        NordeaInvestmentFetcher fetcher = new NordeaInvestmentFetcher(nordeaClient);
        return new InvestmentRefreshController(metricRefreshController, updateController, fetcher);
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                new NordeaCreditCardFetcher(nordeaClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new NordeaCreditCardTransactionFetcher(nordeaClient))));
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        NordeaAccountFetcher fetcher = new NordeaAccountFetcher(nordeaClient);
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                fetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(fetcher).build()));
    }

    private LoanRefreshController constructLoanRefreshController() {
        NordeaDkLoansFetcher fetcher = new NordeaDkLoansFetcher(nordeaClient);
        return new LoanRefreshController(
                metricRefreshController,
                updateController,
                fetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(fetcher)));
    }

    private NordeaDkApiClient constructNordeaClient() {
        return new NordeaDkApiClient(sessionStorage, client, persistentStorage, catalog);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
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

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(
                new NordeaDKIdentityDataFetcher(nordeaClient).fetchIdentityData());
    }
}
