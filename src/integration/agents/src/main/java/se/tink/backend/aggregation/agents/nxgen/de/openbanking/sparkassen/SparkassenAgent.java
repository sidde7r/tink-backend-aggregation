package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.SparkassenAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.SparkassenAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.SparkassenTransactionsFetcher;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class SparkassenAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final SparkassenApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    private final SparkassenPersistentStorage sparkassenPersistentStorage;

    public SparkassenAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());

        sparkassenPersistentStorage = new SparkassenPersistentStorage(persistentStorage);
        String bankCode = request.getProvider().getPayload();
        apiClient = new SparkassenApiClient(client, bankCode, request.isManual(), userIp, provider);
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();

        client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        SparkassenAuthenticator sparkassenAuthenticator =
                new SparkassenAuthenticator(
                        catalog,
                        supplementalInformationHelper,
                        apiClient,
                        sparkassenPersistentStorage,
                        credentials);

        return new AutoAuthenticationController(
                request, context, sparkassenAuthenticator, sparkassenAuthenticator);
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

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new SparkassenAccountsFetcher(apiClient, sparkassenPersistentStorage),
                new SparkassenTransactionsFetcher(apiClient, sparkassenPersistentStorage));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
