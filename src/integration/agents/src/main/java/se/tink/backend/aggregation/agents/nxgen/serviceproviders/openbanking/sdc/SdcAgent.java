package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.Transactions.MAX_CONSECUTIVE_EMPTY_PAGES;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.Transactions.MONTHS_TO_FETCH;

import java.time.temporal.ChronoUnit;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.authenticator.SdcAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.authenticator.SdcAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.configuration.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.SdcTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.SdcTransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class SdcAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final SdcApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public SdcAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());

        apiClient = new SdcApiClient(client, persistentStorage, request.getCredentials());
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();

        apiClient.setConfiguration(
                getAgentConfiguration(), agentsServiceConfiguration.getEidasProxy());
    }

    protected AgentConfiguration<SdcConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController()
                .getAgentCommonConfiguration(SdcConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {

        final SdcAuthenticationController controller =
                new SdcAuthenticationController(
                        supplementalInformationHelper,
                        new SdcAuthenticator(apiClient, persistentStorage),
                        strongAuthenticationState,
                        persistentStorage);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
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
        final SdcTransactionalAccountFetcher accountFetcher =
                new SdcTransactionalAccountFetcher(apiClient);

        final SdcTransactionalAccountTransactionFetcher transactionFetcher =
                new SdcTransactionalAccountTransactionFetcher(apiClient);

        /*
           We are overriding the default date range (by default it is 3 months but we use 1 month)
           See getTransactionsFor method in SdcApiClient class to find out more details
        */

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                transactionFetcher,
                                MAX_CONSECUTIVE_EMPTY_PAGES,
                                MONTHS_TO_FETCH,
                                ChronoUnit.MONTHS)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
