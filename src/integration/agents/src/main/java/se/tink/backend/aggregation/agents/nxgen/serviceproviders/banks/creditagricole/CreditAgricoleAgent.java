package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.CreditAgricoleAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.CreditAgricoleTransactionalAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.filters.CreditAgricoleHttpFilter;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CreditAgricoleAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final CreditAgricoleApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public CreditAgricoleAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);
        persistentStorage.put(StorageKey.REGION_ID, request.getProvider().getPayload());
        apiClient = new CreditAgricoleApiClient(client, persistentStorage, sessionStorage);

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new CreditAgricoleHttpFilter());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new CreditAgricoleAuthenticator(apiClient, sessionStorage, persistentStorage);
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
        CreditAgricoleTransactionalAccountsFetcher transactionalAccountsFetcher =
                new CreditAgricoleTransactionalAccountsFetcher(apiClient);
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                transactionalAccountsFetcher,
                transactionalAccountsFetcher);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new CreditAgricoleSessionHandler(apiClient);
    }
}
