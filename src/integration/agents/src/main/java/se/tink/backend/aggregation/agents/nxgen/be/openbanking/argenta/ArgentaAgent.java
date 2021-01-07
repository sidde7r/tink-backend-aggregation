package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.ArgentaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.configuration.ArgentaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount.ArgentaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount.ArgentaTransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.utils.SignatureHeaderProvider;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class ArgentaAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final ArgentaApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public ArgentaAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());

        super.setConfiguration(agentsServiceConfiguration);

        final AgentConfiguration<ArgentaConfiguration> agentConfiguration =
                getAgentConfigurationController().getAgentConfiguration(ArgentaConfiguration.class);
        final ArgentaConfiguration argentaConfiguration =
                agentConfiguration.getProviderSpecificConfiguration();

        EidasProxyConfiguration eidasProxy = agentsServiceConfiguration.getEidasProxy();
        QsealcSigner qsealcSigner =
                QsealcSignerImpl.build(
                        eidasProxy.toInternalConfig(),
                        QsealcAlg.EIDAS_RSA_SHA256,
                        getEidasIdentity());

        SignatureHeaderProvider signatureHeaderProvider =
                new SignatureHeaderProvider(argentaConfiguration, qsealcSigner);
        apiClient =
                new ArgentaApiClient(
                        client,
                        agentConfiguration,
                        persistentStorage,
                        sessionStorage,
                        signatureHeaderProvider);

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new ArgentaAuthenticator(
                                credentials,
                                apiClient,
                                persistentStorage,
                                supplementalInformationHelper,
                                catalog),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final ArgentaTransactionalAccountFetcher accountFetcher =
                new ArgentaTransactionalAccountFetcher(apiClient);

        final ArgentaTransactionalAccountTransactionFetcher transactionFetcher =
                new ArgentaTransactionalAccountTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(transactionFetcher)
                                .build()));
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
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
