package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.authenticator.SparebankenVestAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.authenticator.SparebankenVestOneTimeActivationCodeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.SparebankenVestCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.SparebankenVestCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.investment.SparebankenVestInvestmentsFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.loan.SparebankenVestLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.SparebankenVestTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.SparebankenVestTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.EncapClient;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.onetimecode.OneTimeActivationCodeAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SparebankenVestAgent extends NextGenerationAgent
        implements RefreshInvestmentAccountsExecutor, RefreshLoanAccountsExecutor {
    private final SparebankenVestApiClient apiClient;
    private final EncapClient encapClient;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;

    public SparebankenVestAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);
        this.apiClient = new SparebankenVestApiClient(client);
        this.encapClient =
                new EncapClient(
                        context,
                        request,
                        signatureKeyPair,
                        persistentStorage,
                        new SparebankenVestEncapConfiguration(),
                        DeviceProfileConfiguration.IOS_STABLE);

        this.investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        SparebankenVestInvestmentsFetcher.create(apiClient));

        this.loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        SparebankenVestLoanFetcher.create(apiClient));
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.setUserAgent(SparebankenVestConstants.Headers.USER_AGENT);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new OneTimeActivationCodeAuthenticationController(
                        SparebankenVestOneTimeActivationCodeAuthenticator.create(
                                apiClient, encapClient)),
                SparebankenVestAutoAuthenticator.create(apiClient, encapClient));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        SparebankenVestTransactionFetcher transactionFetcher =
                SparebankenVestTransactionFetcher.create(apiClient, credentials);

        TransactionFetcherController<TransactionalAccount> transactionFetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(
                                transactionFetcher,
                                SparebankenVestConstants.PagePagination.START_INDEX),
                        transactionFetcher);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        SparebankenVestTransactionalAccountFetcher.create(apiClient, credentials),
                        transactionFetcherController));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {

        TransactionFetcher<CreditCardAccount> transactionFetcher =
                new TransactionFetcherController<CreditCardAccount>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<CreditCardAccount>(
                                SparebankenVestCreditCardTransactionFetcher.create(apiClient)));

        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        SparebankenVestCreditCardAccountFetcher.create(apiClient),
                        transactionFetcher));
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
        return SparebankenVestSessionHandler.create(apiClient);
    }
}
