package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants.AccountHolderType;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.configuration.StarlingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.StarlingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.StarlingTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.oauth.progressive.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class StarlingBusinessAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {
    private final StarlingApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final OAuth2Authenticator authenticator;

    @Inject
    public StarlingBusinessAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        final AgentConfiguration<StarlingConfiguration> agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(StarlingConfiguration.class);
        final StarlingConfiguration starlingConfiguration =
                agentConfiguration.getProviderSpecificConfiguration();
        authenticator =
                new StarlingOAut2Authenticator(
                        persistentStorage,
                        client,
                        starlingConfiguration.getAisConfiguration(),
                        agentConfiguration.getRedirectUrl(),
                        strongAuthenticationState);
        apiClient = new StarlingApiClient(client, persistentStorage);
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
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
                metricRefreshController,
                updateController,
                new StarlingTransactionalAccountFetcher(
                        apiClient,
                        ImmutableSet.of(
                                AccountHolderType.BUSINESS,
                                AccountHolderType.BANKING_AS_A_SERVICE)),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                new StarlingTransactionFetcher(apiClient))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public OAuth2Authenticator getAuthenticator() {
        return authenticator;
    }
}
