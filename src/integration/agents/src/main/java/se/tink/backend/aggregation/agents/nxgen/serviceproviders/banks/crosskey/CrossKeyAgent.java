package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey;

import java.util.NoSuchElementException;
import java.util.Optional;
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
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.CrossKeyInvestmentsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.CrossKeyTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.CrossKeyTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.creditcard.CrossKeyCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.identitydata.CrossKeyIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.loan.CrossKeyLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.sessionhandler.CrossKeySessionHandler;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class CrossKeyAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    protected final CrossKeyApiClient apiClient;
    protected final CrossKeyConfiguration agentConfiguration;
    protected final CrossKeyPersistentStorage agentPersistentStorage;

    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public CrossKeyAgent(
            CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair,
            CrossKeyConfiguration agentConfiguration) {
        super(request, context, signatureKeyPair);
        this.agentConfiguration = agentConfiguration;
        this.apiClient = new CrossKeyApiClient(this.client, agentConfiguration, sessionStorage);
        agentPersistentStorage = new CrossKeyPersistentStorage(this.persistentStorage);

        this.investmentRefreshController =
                new InvestmentRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new CrossKeyInvestmentsFetcher(this.apiClient, agentConfiguration));

        this.loanRefreshController =
                new LoanRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new CrossKeyLoanFetcher(this.apiClient, agentConfiguration));

        this.creditCardRefreshController = constructCreditCardRefreshController();

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
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
        return new TransactionalAccountRefreshController(
                this.metricRefreshController,
                this.updateController,
                new CrossKeyTransactionalAccountFetcher(this.apiClient, agentConfiguration),
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        new CrossKeyTransactionFetcher(
                                                this.apiClient, agentConfiguration))
                                .build()));
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
        CrossKeyCreditCardFetcher creditCardFetcher =
                new CrossKeyCreditCardFetcher(this.apiClient, agentPersistentStorage);
        return new CreditCardRefreshController(
                this.metricRefreshController,
                this.updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(creditCardFetcher)
                                .build()));
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
        return new CrossKeySessionHandler(this.apiClient);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        CrossKeyIdentityDataFetcher fetcher =
                new CrossKeyIdentityDataFetcher(apiClient, agentConfiguration);
        return Optional.of(fetcher.fetchIdentityData())
                .map(FetchIdentityDataResponse::new)
                .orElseThrow(NoSuchElementException::new);
    }
}
