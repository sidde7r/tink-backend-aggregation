package se.tink.backend.aggregation.agents.nxgen.be.banks.axa;

import java.util.Locale;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.ManualOrAutoAuth;
import se.tink.backend.aggregation.agents.ProgressiveAuthAgent;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.AxaAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.AxaManualAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.AxaAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.AxaTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.session.AxaSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.SubsequentGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.ProgressiveAuthController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.ProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class AxaAgent extends SubsequentGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                ProgressiveAuthAgent,
                ManualOrAutoAuth {

    private final AxaApiClient apiClient;
    private final AxaStorage storage;
    private final CredentialsRequest request;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final ProgressiveAuthenticator authenticator;

    public AxaAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);
        this.apiClient = new AxaApiClient(client);
        this.storage = makeStorage();
        this.request = request;

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
        this.authenticator =
                new AutoAuthenticationController(
                        request,
                        systemUpdater,
                        new AxaManualAuthenticator(
                                apiClient, storage, supplementalInformationFormer),
                        new AxaAutoAuthenticator(apiClient, storage));
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.setUserAgent(AxaConstants.Request.USER_AGENT);
        client.setCipherSuites(AxaConstants.CIPHER_SUITES);
    }

    private AxaStorage makeStorage() {
        return new AxaStorage(sessionStorage, persistentStorage);
    }

    private void initRefresh() {
        // Should be updated prior to every refresh
        final String locale = request.getUser().getLocale().replace('_', '-');
        final String language = Locale.forLanguageTag(locale).getLanguage();
        storage.persistLanguage(language);
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
        initRefresh();

        final AccountFetcher<TransactionalAccount> accountFetcher =
                new AxaAccountFetcher(apiClient, storage);
        final TransactionFetcher<TransactionalAccount> transactionFetcher =
                new AxaTransactionFetcher(apiClient, storage);
        return new TransactionalAccountRefreshController(
                metricRefreshController, updateController, accountFetcher, transactionFetcher);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new AxaSessionHandler(apiClient, storage);
    }

    @Override
    public SteppableAuthenticationResponse login(final SteppableAuthenticationRequest request)
            throws Exception {
        return ProgressiveAuthController.of(authenticator, credentials).login(request);
    }

    @Override
    public boolean login() {
        throw new AssertionError(); // ProgressiveAuthAgent::login should always be used
    }

    @Override
    public boolean isManualAuthentication(Credentials credentials) {
        // TODO: remove casting
        return ((ManualOrAutoAuth) authenticator).isManualAuthentication(credentials);
    }
}
