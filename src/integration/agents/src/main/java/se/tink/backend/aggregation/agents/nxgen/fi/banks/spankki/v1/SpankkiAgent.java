package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1;

import java.util.NoSuchElementException;
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
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.authenticator.SpankkiAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.authenticator.SpankkiKeyCardAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.authenticator.entities.CustomerEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.fetcher.creditcard.SpankkiCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.fetcher.investment.SpankkiInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.fetcher.loan.SpankkiLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.fetcher.transactionalaccount.SpankkiTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.fetcher.transactionalaccount.SpankkiTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.sessionhandler.SpankkiSessionHandler;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities(generateFromImplementedExecutors = true)
public final class SpankkiAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {
    private final SpankkiSessionStorage spankkiSessionStorage;
    private final SpankkiPersistentStorage spankkiPersistentStorage;
    private final SpankkiApiClient apiClient;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public SpankkiAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.spankkiSessionStorage = new SpankkiSessionStorage(this.sessionStorage);
        this.spankkiPersistentStorage = new SpankkiPersistentStorage(this.persistentStorage);
        this.apiClient =
                new SpankkiApiClient(
                        this.client, this.spankkiSessionStorage, this.spankkiPersistentStorage);

        SpankkiInvestmentFetcher investmentFetcher = new SpankkiInvestmentFetcher(this.apiClient);
        this.investmentRefreshController =
                new InvestmentRefreshController(
                        this.metricRefreshController, this.updateController, investmentFetcher);

        SpankkiLoanFetcher loanFetcher = new SpankkiLoanFetcher(this.apiClient);
        this.loanRefreshController =
                new LoanRefreshController(
                        this.metricRefreshController, this.updateController, loanFetcher);

        this.creditCardRefreshController = constructCreditCardRefreshController();
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        KeyCardAuthenticationController keyCardCtrl =
                new KeyCardAuthenticationController(
                        this.catalog,
                        this.supplementalInformationHelper,
                        new SpankkiKeyCardAuthenticator(
                                this.apiClient,
                                this.spankkiPersistentStorage,
                                this.spankkiSessionStorage,
                                this.credentials),
                        SpankkiConstants.Authentication.KEY_CARD_VALUE_LENGTH);

        return new AutoAuthenticationController(
                this.request,
                this.context,
                keyCardCtrl,
                new SpankkiAutoAuthenticator(
                        this.apiClient,
                        this.spankkiPersistentStorage,
                        this.spankkiSessionStorage,
                        this.credentials));
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
                new SpankkiTransactionalAccountFetcher(this.apiClient),
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                new SpankkiTransactionFetcher(this.apiClient))));
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
        SpankkiCreditCardFetcher creditcardFetcher = new SpankkiCreditCardFetcher(this.apiClient);
        return new CreditCardRefreshController(
                this.metricRefreshController,
                this.updateController,
                creditcardFetcher,
                creditcardFetcher);
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
        return new SpankkiSessionHandler(this.apiClient);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return spankkiSessionStorage
                .getCustomerEntity()
                .map(CustomerEntity::toTinkIdentity)
                .map(FetchIdentityDataResponse::new)
                .orElseThrow(NoSuchElementException::new);
    }
}
