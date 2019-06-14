package se.tink.backend.aggregation.nxgen.agents.demo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
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
        implements RefreshIdentityDataExecutor {
    private final NextGenerationDemoAuthenticator authenticator;
    // TODO Requires changes when multi-currency is implemented. Will do for now
    protected final String currency;

    public NextGenerationDemoAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.authenticator = new NextGenerationDemoAuthenticator(credentials);
        this.currency = request.getProvider().getCurrency();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new TypedAuthenticationController(
                new BankIdAuthenticationController<>(supplementalRequester, authenticator),
                new PasswordAuthenticationController(authenticator));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        NextGenerationDemoTransactionFetcher transactionFetcher =
                new NextGenerationDemoTransactionFetcher(
                        request.getAccounts(),
                        currency,
                        catalog,
                        getTransactionAccounts(),
                        getDemoSavingsAccounts());

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        transactionFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper, transactionFetcher)));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        NextGenerationDemoCreditCardFetcher transactionAndAccountFetcher =
                new NextGenerationDemoCreditCardFetcher(
                        request.getAccounts(), currency, catalog, getCreditCardAccounts());

        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        transactionAndAccountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper, transactionAndAccountFetcher)));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new NextGenerationDemoInvestmentFetcher(
                                currency, getInvestmentAccounts())));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new NextGenerationDemoLoanFetcher(
                                currency, catalog, getDemoLoanAccounts())));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new NextGenerationDemoSession();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(
                IdentityData.builder()
                        .setFullName("Jane Doe")
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
