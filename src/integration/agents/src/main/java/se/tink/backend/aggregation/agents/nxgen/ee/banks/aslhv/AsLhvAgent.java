package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.authenticator.AsLhvPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.fetcher.creditcard.AsLhvCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.fetcher.creditcard.AsLhvCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.fetcher.transactionalaccount.AsLhvTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.fetcher.transactionalaccount.AsLhvTransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.session.AsLhvSessionHandler;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
public final class AsLhvAgent extends NextGenerationAgent
        implements RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    private final AsLhvApiClient apiClient;
    private final AsLhvSessionStorage asLhvSessionStorage;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public AsLhvAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        asLhvSessionStorage = new AsLhvSessionStorage(sessionStorage);
        this.apiClient = new AsLhvApiClient(this.client);

        this.creditCardRefreshController = constructCreditCardRefreshController();
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new AsLhvPasswordAuthenticator(apiClient, asLhvSessionStorage));
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
        final AsLhvTransactionalAccountTransactionFetcher transactionFetcher =
                new AsLhvTransactionalAccountTransactionFetcher(apiClient, asLhvSessionStorage);
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new AsLhvTransactionalAccountFetcher(apiClient, asLhvSessionStorage),
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(transactionFetcher)
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
        final AsLhvCreditCardTransactionFetcher transactionFetcher =
                new AsLhvCreditCardTransactionFetcher(apiClient, asLhvSessionStorage);
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                new AsLhvCreditCardAccountFetcher(apiClient, asLhvSessionStorage),
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(transactionFetcher)
                                .build()));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new AsLhvSessionHandler(this.apiClient);
    }
}
