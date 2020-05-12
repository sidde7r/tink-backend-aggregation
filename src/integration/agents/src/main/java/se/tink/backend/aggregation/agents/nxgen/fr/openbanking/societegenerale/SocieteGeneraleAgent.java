package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.authenticator.SocieteGeneraleAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.configuration.SocieteGeneraleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.SocieteGeneraleIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.SocieteGeneraleTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.SocieteGeneraleTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.utils.SignatureHeaderProvider;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
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

public final class SocieteGeneraleAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor {

    private final SocieteGeneraleApiClient apiClient;
    private final SocieteGeneraleAuthenticator authenticator;
    private TransactionalAccountRefreshController transactionalAccountRefreshController;
    private SocieteGeneraleIdentityDataFetcher societeGeneraleIdentityDataFetcher;
    private SignatureHeaderProvider signatureHeaderProvider;

    @Inject
    public SocieteGeneraleAgent(
            AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);
        final SocieteGeneraleConfiguration societeGeneraleConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(SocieteGeneraleConfiguration.class);

        apiClient =
                new SocieteGeneraleApiClient(
                        client, persistentStorage, societeGeneraleConfiguration);

        authenticator =
                new SocieteGeneraleAuthenticator(
                        apiClient, persistentStorage, societeGeneraleConfiguration);

        signatureHeaderProvider =
                new SignatureHeaderProvider(qsealcSigner, societeGeneraleConfiguration);

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();

        societeGeneraleIdentityDataFetcher =
                new SocieteGeneraleIdentityDataFetcher(
                        apiClient, sessionStorage, signatureHeaderProvider);
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        authenticator,
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
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
        SocieteGeneraleTransactionalAccountFetcher accountFetcher =
                new SocieteGeneraleTransactionalAccountFetcher(
                        apiClient, sessionStorage, signatureHeaderProvider);

        SocieteGeneraleTransactionFetcher transactionFetcher =
                new SocieteGeneraleTransactionFetcher(
                        apiClient, sessionStorage, signatureHeaderProvider);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return societeGeneraleIdentityDataFetcher.response();
    }
}
