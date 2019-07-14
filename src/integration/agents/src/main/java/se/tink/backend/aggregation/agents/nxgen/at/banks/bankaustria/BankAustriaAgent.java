package se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.authenticator.BankAustriaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.fetcher.BankAustriaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.otml.OtmlBodyReader;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.otml.OtmlResponseConverter;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.sessionhandler.BankAustriaSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BankAustriaAgent extends NextGenerationAgent implements RefreshCheckingAccountsExecutor,
        RefreshSavingsAccountsExecutor {

    private final OtmlResponseConverter otmlResponseConverter;
    private final BankAustriaSessionStorage bankAustriaSessionStorage;
    private BankAustriaApiClient apiClient;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public BankAustriaAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);
        this.bankAustriaSessionStorage =
                new BankAustriaSessionStorage(
                        this.sessionStorage,
                        BankAustriaConstants.Device.IPHONE7_OTML_LAYOUT_INITIAL);
        this.apiClient = new BankAustriaApiClient(this.client, bankAustriaSessionStorage);
        this.otmlResponseConverter = new OtmlResponseConverter();

        this.transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.addMessageReader(new OtmlBodyReader<>());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        BankAustriaAuthenticator authenticator =
                new BankAustriaAuthenticator(
                        this.apiClient,
                        this.credentials,
                        this.persistentStorage,
                        bankAustriaSessionStorage,
                        otmlResponseConverter);

        return new PasswordAuthenticationController(authenticator);
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
        BankAustriaTransactionalAccountFetcher transactionalAccountFetcher =
                new BankAustriaTransactionalAccountFetcher(apiClient, otmlResponseConverter);
        return new TransactionalAccountRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        transactionalAccountFetcher,
                        new TransactionFetcherController<>(
                                this.transactionPaginationHelper,
                                new TransactionDatePaginationController<>(
                                        transactionalAccountFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new BankAustriaSessionHandler(apiClient, otmlResponseConverter);
    }
}
