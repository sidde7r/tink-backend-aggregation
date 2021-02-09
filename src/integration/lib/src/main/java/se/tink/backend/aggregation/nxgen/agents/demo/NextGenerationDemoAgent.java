package se.tink.backend.aggregation.nxgen.agents.demo;

import java.time.LocalDate;
import java.util.List;
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
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoCreditCardAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoIdentityData;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoInvestmentAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoLoanAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoTransactionAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.fetchers.NextGenerationDemoCreditCardFetcher;
import se.tink.backend.aggregation.nxgen.agents.demo.fetchers.NextGenerationDemoInvestmentFetcher;
import se.tink.backend.aggregation.nxgen.agents.demo.fetchers.NextGenerationDemoLoanFetcher;
import se.tink.backend.aggregation.nxgen.agents.demo.fetchers.NextGenerationDemoTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.identitydata.IdentityData;

public abstract class NextGenerationDemoAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {
    private final NextGenerationDemoAuthenticator authenticator;
    // TODO Requires changes when multi-currency is implemented. Will do for now
    protected final String currency;
    private InvestmentRefreshController investmentRefreshController;
    private LoanRefreshController loanRefreshController;
    private CreditCardRefreshController creditCardRefreshController;
    private TransactionalAccountRefreshController transactionalAccountRefreshController;

    public NextGenerationDemoAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.authenticator = new NextGenerationDemoAuthenticator(credentials);
        this.currency = request.getProvider().getCurrency();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new TypedAuthenticationController(
                new BankIdAuthenticationController<>(
                        supplementalInformationController,
                        authenticator,
                        persistentStorage,
                        credentials),
                new PasswordAuthenticationController(authenticator));
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return lazyLoadTransactionalAccountRefreshController().fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return lazyLoadTransactionalAccountRefreshController().fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return lazyLoadTransactionalAccountRefreshController().fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return lazyLoadTransactionalAccountRefreshController().fetchSavingsTransactions();
    }

    private TransactionalAccountRefreshController lazyLoadTransactionalAccountRefreshController() {
        if (transactionalAccountRefreshController != null) {
            return transactionalAccountRefreshController;
        }

        NextGenerationDemoTransactionFetcher transactionFetcher =
                new NextGenerationDemoTransactionFetcher(
                        request.getAccounts(),
                        currency,
                        catalog,
                        getTransactionAccounts(),
                        getDemoSavingsAccounts());

        transactionalAccountRefreshController =
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        transactionFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper, transactionFetcher));

        return transactionalAccountRefreshController;
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return lazyLoadCreditCardRefreshController().fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return lazyLoadCreditCardRefreshController().fetchCreditCardTransactions();
    }

    private CreditCardRefreshController lazyLoadCreditCardRefreshController() {
        if (creditCardRefreshController != null) {
            return creditCardRefreshController;
        }
        NextGenerationDemoCreditCardFetcher transactionAndAccountFetcher =
                new NextGenerationDemoCreditCardFetcher(
                        request.getAccounts(), currency, catalog, getCreditCardAccounts());

        creditCardRefreshController =
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        transactionAndAccountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper, transactionAndAccountFetcher));
        return creditCardRefreshController;
    }

    private InvestmentRefreshController lazyLoadInvestmentRefreshController() {
        if (investmentRefreshController != null) {
            return investmentRefreshController;
        }

        investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new NextGenerationDemoInvestmentFetcher(currency, getInvestmentAccounts()));

        return investmentRefreshController;
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return lazyLoadInvestmentRefreshController().fetchInvestmentAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return lazyLoadInvestmentRefreshController().fetchInvestmentTransactions();
    }

    private LoanRefreshController lazyLoadLoanRefreshController() {
        if (loanRefreshController != null) {
            return loanRefreshController;
        }

        loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new NextGenerationDemoLoanFetcher(
                                currency, catalog, getDemoLoanAccounts()));

        return loanRefreshController;
    }

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return lazyLoadLoanRefreshController().fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return lazyLoadLoanRefreshController().fetchLoanTransactions();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new NextGenerationDemoSession();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(
                IdentityData.builder()
                        .addFirstNameElement("Jane")
                        .addSurnameElement("Doe")
                        .setDateOfBirth(LocalDate.now())
                        .build());
    }

    public abstract DemoInvestmentAccount getInvestmentAccounts();

    public abstract DemoSavingsAccount getDemoSavingsAccounts();

    public abstract DemoLoanAccount getDemoLoanAccounts();

    public abstract List<DemoTransactionAccount> getTransactionAccounts();

    public abstract List<DemoCreditCardAccount> getCreditCardAccounts();

    public abstract DemoIdentityData getIdentityDataResponse();
}
