package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys;

import com.google.common.base.Strings;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator.RedsysAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator.RedsysAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration.AspspConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration.RedsysConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.RedsysConsentController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.RedsysConsentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.RedsysTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.RedsysUpcomingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.BaseTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class RedsysAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                AspspConfiguration {

    private final RedsysApiClient apiClient;
    private final RedsysConsentStorage consentStorage;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final RedsysConsentController consentController;

    public RedsysAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration) {
        super(request, context, configuration.getSignatureKeyPair());

        apiClient =
                new RedsysApiClient(
                        client, sessionStorage, persistentStorage, this, this.getEidasIdentity());
        apiClient.setPsuIpAddress(Strings.emptyToNull(request.getOriginatingUserIp()));

        consentStorage = new RedsysConsentStorage(persistentStorage);
        consentController =
                new RedsysConsentController(
                        apiClient,
                        consentStorage,
                        supplementalInformationHelper,
                        strongAuthenticationState);

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        final AgentConfiguration<RedsysConfiguration> agentConfiguration =
                getAgentConfigurationController().getAgentConfiguration(RedsysConfiguration.class);
        apiClient.setConfiguration(agentConfiguration, configuration.getEidasProxy());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final RedsysAuthenticationController redsysAuthenticationController =
                new RedsysAuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new RedsysAuthenticator(apiClient, sessionStorage),
                        consentController,
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        redsysAuthenticationController, supplementalInformationHelper),
                redsysAuthenticationController);
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
        final RedsysTransactionalAccountFetcher accountFetcher =
                new RedsysTransactionalAccountFetcher(apiClient, consentController, this);

        final TransactionPaginator<TransactionalAccount> paginator =
                new TransactionKeyPaginationController<>(accountFetcher);

        final TransactionFetcherController<TransactionalAccount> controller;
        if (supportsPendingTransactions()) {
            final RedsysUpcomingTransactionFetcher upcomingTransactionFetcher =
                    new RedsysUpcomingTransactionFetcher(apiClient, consentController);
            controller =
                    new TransactionFetcherController<>(
                            transactionPaginationHelper, paginator, upcomingTransactionFetcher);
        } else {
            controller = new TransactionFetcherController<>(transactionPaginationHelper, paginator);
        }

        return new TransactionalAccountRefreshController(
                metricRefreshController, updateController, accountFetcher, controller);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public LocalDate oldestTransactionDate() {
        return LocalDate.now().minusYears(2);
    }

    @Override
    public Class<? extends BaseTransactionsResponse> getTransactionsResponseClass() {
        return TransactionsResponse.class;
    }

    @Override
    public boolean shouldReturnLowercaseAccountId() {
        return false;
    }
}
