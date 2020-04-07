package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.authenticator.AbancaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.configuration.AbancaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.AbancaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.AbancaTransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.session.AbancaSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public final class AbancaAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final AbancaApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final AbancaConfiguration abancaConfiguration;

    public AbancaAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);

        abancaConfiguration =
                getAgentConfigurationController().getAgentConfiguration(AbancaConfiguration.class);
        apiClient =
                new AbancaApiClient(
                        client,
                        agentComponentProvider.getContext().getCatalog(),
                        abancaConfiguration,
                        sessionStorage,
                        credentials,
                        supplementalRequester);
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final AbancaAuthenticator abancaAuthenticator =
                new AbancaAuthenticator(apiClient, abancaConfiguration);
        final OAuth2AuthenticationController oAuth2AuthenticationController =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        abancaAuthenticator,
                        credentials,
                        strongAuthenticationState);
        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        oAuth2AuthenticationController, supplementalInformationHelper),
                oAuth2AuthenticationController);
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
        AbancaTransactionalAccountFetcher accountFetcher =
                new AbancaTransactionalAccountFetcher(apiClient);

        AbancaTransactionalAccountTransactionFetcher transactionFetcher =
                new AbancaTransactionalAccountTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new AbancaSessionHandler(apiClient);
    }
}
