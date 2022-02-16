package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.LIST_BENEFICIARIES;

import com.google.inject.Inject;
import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshBeneficiariesExecutor;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.apiclient.ArkeaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.apiclient.ArkeaSignatureHeaderCreator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.authenticator.ArkeaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.configuration.ArkeaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.fetcher.ArkeaEndUserIdentityFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.fetcher.ArkeaTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.fetcher.ArkeaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrTransferDestinationFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.LoggingStrategy;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS, IDENTITY_DATA, LIST_BENEFICIARIES})
public class ArkeaAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshIdentityDataExecutor,
                RefreshBeneficiariesExecutor {

    private final AgentConfiguration<ArkeaConfiguration> agentConfiguration;
    private final ArkeaAuthenticator authenticator;
    private final ArkeaApiClient apiClient;
    private final ArkeaSignatureHeaderCreator signatureHeaderCreator;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final ArkeaEndUserIdentityFetcher endUserIdentityFetcher;
    private final TransferDestinationRefreshController transferDestinationRefreshController;

    @Inject
    public ArkeaAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {

        super(componentProvider);

        setJsonHttpTrafficLogsEnabled(true);
        client.setLoggingStrategy(LoggingStrategy.EXPERIMENTAL);

        agentConfiguration =
                getAgentConfigurationController().getAgentConfiguration(ArkeaConfiguration.class);
        signatureHeaderCreator =
                new ArkeaSignatureHeaderCreator(
                        qsealcSigner,
                        agentConfiguration.getProviderSpecificConfiguration().getQsealcUrl());
        apiClient = new ArkeaApiClient(client, persistentStorage, signatureHeaderCreator);
        authenticator = new ArkeaAuthenticator(apiClient, persistentStorage, agentConfiguration);
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        endUserIdentityFetcher = new ArkeaEndUserIdentityFetcher(apiClient);
        transferDestinationRefreshController = getTransferDestinationRefreshController();
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
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
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
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(endUserIdentityFetcher.fetchIdentityData());
    }

    @Override
    public FetchTransferDestinationsResponse fetchBeneficiaries(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new ArkeaTransactionalAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new ArkeaTransactionFetcher<>(apiClient))));
    }

    private TransferDestinationRefreshController getTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new FrTransferDestinationFetcher(apiClient));
    }
}
