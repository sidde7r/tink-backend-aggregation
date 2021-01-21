package se.tink.backend.aggregation.agents.nxgen.be.banks.axa;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.util.Locale;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.AxaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.AxaAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.AxaTransactionFetcher;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({SAVINGS_ACCOUNTS})
public final class AxaAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                ProgressiveAuthAgent {

    private final AxaApiClient apiClient;
    private final AxaStorage storage;
    private final CredentialsRequest request;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private AxaAuthenticator authenticator;

    @Inject
    public AxaAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        this.storage = new AxaStorage(sessionStorage, persistentStorage);
        this.apiClient = new AxaApiClient(client, storage);
        this.request = agentComponentProvider.getCredentialsRequest();
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
        storage.persistCardNumber(request.getCredentials().getField(Key.USERNAME));
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
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        if (authenticator == null) {
            authenticator = new AxaAuthenticator(storage, apiClient, supplementalInformationFormer);
        }

        return authenticator;
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

    private void initRefresh() {
        // Should be updated prior to every refresh
        final String locale = request.getUser().getLocale().replace('_', '-');
        final String language = Locale.forLanguageTag(locale).getLanguage();
        storage.persistLanguage(language);
    }
}
