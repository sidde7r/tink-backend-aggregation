package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1;

import com.google.common.base.Preconditions;
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
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Headers;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Keys;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.Sparebank1Authenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication.RestRootResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.FinancialInstitutionEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.Sparebank1CreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.Sparebank1CreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.Sparebank1InvestmentsFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.Sparebank1LoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.Sparebank1TransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.Sparebank1TransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.rpc.filters.AddRefererFilter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.sessionhandler.Sparebank1SessionHandler;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticationControllerNO;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class Sparebank1Agent extends NextGenerationAgent
        implements RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {
    private final Sparebank1ApiClient apiClient;
    private final RestRootResponse restRootResponse;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public Sparebank1Agent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);

        String bankId = request.getProvider().getPayload();
        sessionStorage.put(Keys.BANK_ID, bankId);
        apiClient = new Sparebank1ApiClient(client, bankId);
        FinancialInstitutionEntity financialInstitution = apiClient.getFinancialInstitution();
        restRootResponse = getRestRootResponse(financialInstitution);

        investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new Sparebank1InvestmentsFetcher(apiClient));

        loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new Sparebank1LoanFetcher(apiClient));

        creditCardRefreshController = constructCreditCardRefreshController();
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    private RestRootResponse getRestRootResponse(FinancialInstitutionEntity financialInstitution) {
        LinkEntity restRootLink =
                Preconditions.checkNotNull(
                        financialInstitution.getLinks().get(Keys.REST_ROOT_KEY),
                        "Link to the rest root not found");

        return apiClient.get(restRootLink.getHref(), RestRootResponse.class);
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.setUserAgent(Headers.USER_AGENT);
        AddRefererFilter filter = new AddRefererFilter();
        client.addFilter(filter);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        Sparebank1Authenticator authenticator =
                new Sparebank1Authenticator(
                        apiClient, credentials, persistentStorage, restRootResponse);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new BankIdAuthenticationControllerNO(supplementalRequester, authenticator),
                authenticator);
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
                metricRefreshController,
                updateController,
                new Sparebank1TransactionalAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper, new Sparebank1TransactionFetcher(apiClient)));
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
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                new Sparebank1CreditCardFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new Sparebank1CreditCardTransactionFetcher(apiClient))));
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
        return new Sparebank1SessionHandler(apiClient, restRootResponse);
    }
}
