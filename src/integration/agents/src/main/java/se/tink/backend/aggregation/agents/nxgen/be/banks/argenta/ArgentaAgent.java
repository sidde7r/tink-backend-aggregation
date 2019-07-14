package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta;

import static org.apache.http.client.config.CookieSpecs.IGNORE_COOKIES;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.ArgentaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.ArgentaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.ArgentaTransactionalTransactionFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class ArgentaAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final ArgentaApiClient apiClient;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public ArgentaAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);
        this.apiClient =
                new ArgentaApiClient(this.client, new ArgentaSessionStorage(this.sessionStorage));

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    protected void configureHttpClient(TinkHttpClient client) {
        // Argenta tries to set "out of domain cookies", to avoid a warning for each request just
        // ignore cookies.
        client.setCookieSpec(IGNORE_COOKIES);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        ArgentaPersistentStorage argentaPersistentStorage =
                new ArgentaPersistentStorage(this.persistentStorage);
        ArgentaAuthenticator argentaAuthenticator =
                new ArgentaAuthenticator(
                        argentaPersistentStorage,
                        apiClient,
                        credentials,
                        supplementalInformationHelper);

        return new AutoAuthenticationController(
                request, systemUpdater, argentaAuthenticator, argentaAuthenticator);
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
        String deviceId = new ArgentaPersistentStorage(this.persistentStorage).getDeviceId();
        ArgentaTransactionalAccountFetcher transactionalAccountFetcher =
                new ArgentaTransactionalAccountFetcher(apiClient, deviceId);
        ArgentaTransactionalTransactionFetcher transactionalTransactionFetcher =
                new ArgentaTransactionalTransactionFetcher(apiClient, deviceId);

        TransactionPagePaginationController<TransactionalAccount>
                transactionPagePaginationController =
                        new TransactionPagePaginationController<>(
                                transactionalTransactionFetcher,
                                ArgentaConstants.Fetcher.START_PAGE);
        TransactionFetcherController<TransactionalAccount> transactionFetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper, transactionPagePaginationController);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                transactionalAccountFetcher,
                transactionFetcherController);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new ArgentaSessionHandler(
                apiClient, new ArgentaPersistentStorage(this.persistentStorage));
    }
}
