package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.SodexoConstants.HttpClient.MAX_RETRIES;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.SodexoConstants.HttpClient.RETRY_SLEEP_MILLISECONDS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.fetchers.SodexoAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.fetchers.SodexoTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.filter.SodexoClientConfigurator;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({CHECKING_ACCOUNTS})
public class SodexoAgent extends NextGenerationAgent implements RefreshCheckingAccountsExecutor {

    private final SodexoApiClient sodexoApiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final SodexoStorage sodexoStorage;
    private final RandomValueGenerator randomValueGenerator;

    @Inject
    public SodexoAgent(final AgentComponentProvider componentProvider) {
        super(componentProvider);

        configureHttpClient();
        this.sodexoStorage = new SodexoStorage(persistentStorage, sessionStorage);
        this.sodexoApiClient = new SodexoApiClient(client, sodexoStorage);

        this.transactionalAccountRefreshController =
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        new SodexoAccountsFetcher(sodexoApiClient, sodexoStorage),
                        new SodexoTransactionsFetcher(sodexoApiClient));
        this.randomValueGenerator = componentProvider.getRandomValueGenerator();
    }

    private void configureHttpClient() {
        new SodexoClientConfigurator().applyFilters(client, MAX_RETRIES, RETRY_SLEEP_MILLISECONDS);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new SodexoAuthenticator(sodexoApiClient, sodexoStorage, randomValueGenerator));
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }
}
