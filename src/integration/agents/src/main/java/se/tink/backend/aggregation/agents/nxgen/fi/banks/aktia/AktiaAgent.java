package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia;

import static se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaConstants.HttpHeaders.SHOULD_SENT_X_SIGNATURE_HEADER;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.util.NoSuchElementException;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaConstants.InstanceStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.AktiaAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.AktiaEncapConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.AktiaKeyCardAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.AktiaSmsAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.entities.UserAccountInfo;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.transactionalaccount.AktiaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapClient;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.module.EncapClientModule;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.module.EncapClientProvider;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycardandsmsotp.KeyCardAndSmsOtpAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.MultiIpGateway;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.identitydata.IdentityData;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, IDENTITY_DATA})
@AgentDependencyModules(modules = EncapClientModule.class)
public final class AktiaAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {
    private final AktiaApiClient apiClient;
    private final EncapClient encapClient;
    private final Storage instanceStorage;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public AktiaAgent(
            AgentComponentProvider agentComponentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration,
            EncapClientProvider encapClientProvider) {
        super(agentComponentProvider);
        configureHttpClient(client, agentsServiceConfiguration);

        this.apiClient = new AktiaApiClient(client);
        this.instanceStorage = new Storage();

        this.encapClient =
                encapClientProvider.getEncapClient(
                        persistentStorage,
                        new AktiaEncapConfiguration(),
                        AktiaConstants.DEVICE_PROFILE,
                        client,
                        SHOULD_SENT_X_SIGNATURE_HEADER);

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    private void configureHttpClient(
            TinkHttpClient client, AgentsServiceConfiguration agentsServiceConfiguration) {
        client.setUserAgent(AktiaConstants.HttpHeaders.USER_AGENT);
        client.disableAggregatorHeader();
        client.disableSignatureRequestHeader();
        final MultiIpGateway gateway =
                new MultiIpGateway(client, credentials.getUserId(), credentials.getId());
        gateway.setMultiIpGateway(agentsServiceConfiguration.getIntegrations());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new KeyCardAndSmsOtpAuthenticationController<>(
                        catalog,
                        supplementalInformationHelper,
                        new AktiaKeyCardAuthenticator(apiClient, encapClient, credentials),
                        6,
                        new AktiaSmsAuthenticator(apiClient, encapClient, instanceStorage),
                        6),
                new AktiaAutoAuthenticator(apiClient, encapClient, instanceStorage));
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
        AktiaTransactionalAccountFetcher aktiaTransactionalAccountFetcher =
                new AktiaTransactionalAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                aktiaTransactionalAccountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                aktiaTransactionalAccountFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        final IdentityData identityData =
                instanceStorage
                        .get(InstanceStorage.USER_ACCOUNT_INFO, UserAccountInfo.class)
                        .orElseThrow(NoSuchElementException::new)
                        .toIdentityData();
        return new FetchIdentityDataResponse(identityData);
    }
}
