package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia;

import java.util.NoSuchElementException;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaConstants.InstanceStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.AktiaAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.AktiaEncapConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.AktiaKeyCardAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.entities.UserAccountInfo;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.transactionalaccount.AktiaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.EncapClient;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.identitydata.IdentityData;

public class AktiaAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {
    private final AktiaApiClient apiClient;
    private final EncapClient encapClient;
    private final Storage instanceStorage;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public AktiaAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);

        this.apiClient = new AktiaApiClient(client);
        this.instanceStorage = new Storage();

        this.encapClient =
                new EncapClient(
                        context,
                        request,
                        signatureKeyPair,
                        persistentStorage,
                        new AktiaEncapConfiguration(),
                        AktiaConstants.DEVICE_PROFILE);

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.setUserAgent(AktiaConstants.HttpHeaders.USER_AGENT);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new KeyCardAuthenticationController(
                        catalog,
                        supplementalInformationHelper,
                        new AktiaKeyCardAuthenticator(apiClient, encapClient, instanceStorage)),
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
        return new AktiaSessionHandler();
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
