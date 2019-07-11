package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.SabadellAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities.InitiateSessionRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.SabadellCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.SabadellCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.SabadellInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.loans.SabadellLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.SabadellAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.SabadellTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.session.SabadellSessionHandler;
import se.tink.backend.aggregation.agents.utils.encoding.messagebodywriter.NoEscapeOfBackslashMessageBodyWriter;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SabadellAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor {
    private final SabadellApiClient apiClient;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;

    public SabadellAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);
        this.apiClient = new SabadellApiClient(client);

        this.investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new SabadellInvestmentFetcher(apiClient));

        this.loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new SabadellLoanFetcher(apiClient));
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.addMessageWriter(
                new NoEscapeOfBackslashMessageBodyWriter(InitiateSessionRequestEntity.class));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new SabadellAuthenticator(apiClient, sessionStorage));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        new SabadellAccountFetcher(apiClient),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(
                                        new SabadellTransactionFetcher(apiClient)))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        new SabadellCreditCardFetcher(apiClient),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionPagePaginationController<>(
                                        new SabadellCreditCardTransactionFetcher(apiClient), 1))));
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
        return new SabadellSessionHandler(apiClient);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return sessionStorage
                .get(Storage.SESSION_KEY, SessionResponse.class)
                .orElseThrow(IllegalStateException::new)
                .getUser()
                .toTinkIdentity();
    }
}
