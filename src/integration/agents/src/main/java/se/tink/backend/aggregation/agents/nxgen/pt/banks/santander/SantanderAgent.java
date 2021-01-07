package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import java.time.LocalDate;
import java.util.Map;
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
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.authenticator.SantanderPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.authenticator.SantanderSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.SantanderApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.Fields.Identity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.SantanderCreditAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.SantanderCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.SantanderInvestmentAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.SantanderInvestmentTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.SantanderLoanAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.SantanderTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.SantanderTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.SubsequentGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.ProductionAgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.identitydata.IdentityData;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    SAVINGS_ACCOUNTS,
    IDENTITY_DATA,
    CREDIT_CARDS,
    INVESTMENTS,
    LOANS
})
public final class SantanderAgent
        extends SubsequentGenerationAgent<PasswordAuthenticationController>
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshLoanAccountsExecutor {

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final SantanderApiClient apiClient;
    private final PasswordAuthenticationController authenticator;

    public SantanderAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(ProductionAgentComponentProvider.create(request, context, signatureKeyPair));

        apiClient = new SantanderApiClient(client, sessionStorage);
        authenticator =
                new PasswordAuthenticationController(
                        new SantanderPasswordAuthenticator(apiClient, sessionStorage));

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();

        this.creditCardRefreshController =
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        new SantanderCreditAccountFetcher(apiClient),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionPagePaginationController<>(
                                        new SantanderCreditCardTransactionFetcher(apiClient), 1)));

        this.investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new SantanderInvestmentAccountFetcher(apiClient),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionPagePaginationController<>(
                                        new SantanderInvestmentTransactionFetcher(apiClient), 1)));

        this.loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new SantanderLoanAccountFetcher(apiClient));
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new SantanderTransactionalAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        new SantanderTransactionFetcher(apiClient))
                                .build()));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SantanderSessionHandler(apiClient, sessionStorage);
    }

    @Override
    public PasswordAuthenticationController getAuthenticator() {
        return authenticator;
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

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        Map<String, String> obj =
                (Map<String, String>) apiClient.fetchIdentityData().getBusinessData().get(0);
        return new FetchIdentityDataResponse(
                IdentityData.builder()
                        .setFullName(obj.get(Identity.USER_NAME))
                        .setDateOfBirth(LocalDate.parse(obj.get(Identity.BIRTH_DATE)))
                        .build());
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
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
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
    public boolean login() throws Exception {
        getAuthenticator().authenticate(credentials);
        return true;
    }
}
