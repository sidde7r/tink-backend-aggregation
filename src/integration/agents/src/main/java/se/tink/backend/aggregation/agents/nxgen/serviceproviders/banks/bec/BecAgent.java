package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.BecAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.BecAccountTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard.BecCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard.BecCreditCardTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.BecAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.filter.BecFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.BecInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.BecLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.session.BecSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BecAgent extends NextGenerationAgent implements RefreshInvestmentAccountsExecutor {
    private final BecApiClient apiClient;
    private final BecAccountTransactionsFetcher transactionFetcher;
    private final InvestmentRefreshController investmentRefreshController;

    public BecAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.client.addFilter(new BecFilter());
        this.apiClient =
                new BecApiClient(
                        this.client, new BecUrlConfiguration(request.getProvider().getPayload()));
        this.transactionFetcher = new BecAccountTransactionsFetcher(this.apiClient);
        this.investmentRefreshController =
                new InvestmentRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new BecInvestmentFetcher(this.apiClient));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(new BecAuthenticator(this.apiClient));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new BecAccountFetcher(this.apiClient),
                        new TransactionFetcherController<>(
                                this.transactionPaginationHelper,
                                new TransactionDatePaginationController<>(this.transactionFetcher),
                                this.transactionFetcher)));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.of(
                new CreditCardRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new BecCreditCardFetcher(this.apiClient),
                        new TransactionFetcherController<>(
                                this.transactionPaginationHelper,
                                new TransactionDatePaginationController<>(
                                        new BecCreditCardTransactionsFetcher(this.apiClient)))));
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
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(
                new LoanRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new BecLoanFetcher(this.apiClient, this.credentials)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new BecSessionHandler(this.apiClient);
    }
}
