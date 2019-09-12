package se.tink.backend.aggregation.agents.nxgen.se.banks.seb;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.SebAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.creditcard.SebCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.TransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.TransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.session.SebSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SebAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor,
                RefreshCreditCardAccountsExecutor {
    private final SebApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final SebSessionStorage sebSessionStorage;
    private final CreditCardRefreshController creditCardRefreshController;

    public SebAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new SebApiClient(client);
        sebSessionStorage = new SebSessionStorage(sessionStorage);
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
        creditCardRefreshController = constructCreditCardRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(
                supplementalRequester,
                new SebAuthenticator(apiClient, sebSessionStorage),
                persistentStorage,
                credentials);
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        SebCreditCardFetcher cardFetcher = new SebCreditCardFetcher(apiClient, sebSessionStorage);
        return new CreditCardRefreshController(
                metricRefreshController, updateController, cardFetcher, cardFetcher);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new TransactionalAccountFetcher(apiClient, sebSessionStorage),
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(new TransactionFetcher(apiClient)),
                        new UpcomingTransactionFetcher(apiClient)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SebSessionHandler(apiClient, sebSessionStorage);
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
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(sebSessionStorage.getIdentityData());
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }
}
