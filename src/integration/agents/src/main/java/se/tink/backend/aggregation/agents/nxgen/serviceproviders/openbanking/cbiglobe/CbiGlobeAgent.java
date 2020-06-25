package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe;

import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.InstrumentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.CbiGlobePaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.CbiGlobeTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.ProductionAgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProviderImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.AccessExceededFilter;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class CbiGlobeAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshTransferDestinationExecutor {

    protected CbiGlobeApiClient apiClient;
    protected TransactionalAccountRefreshController transactionalAccountRefreshController;
    protected TemporaryStorage temporaryStorage;
    protected StatelessProgressiveAuthenticator authenticator;
    protected CbiUserState userState;

    public CbiGlobeAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(ProductionAgentComponentProvider.create(request, context, signatureKeyPair));

        temporaryStorage = new TemporaryStorage();
        apiClient = getApiClient(request.isManual());
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        userState = new CbiUserState(persistentStorage, sessionStorage, credentials);
        authenticator = getAuthenticator();

        applyFilters(this.client);
    }

    private void applyFilters(TinkHttpClient client) {
        client.addFilter(new AccessExceededFilter());
    }

    protected CbiGlobeApiClient getApiClient(boolean requestManual) {
        return new CbiGlobeApiClient(
                client,
                persistentStorage,
                sessionStorage,
                requestManual,
                temporaryStorage,
                InstrumentType.ACCOUNTS);
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        final AgentConfiguration<CbiGlobeConfiguration> agentConfiguration =
                getAgentConfiguration();
        apiClient.setConfiguration(agentConfiguration);
        this.client.setDebugOutput(true);
        this.client.setEidasProxy(configuration.getEidasProxy());
    }

    protected AgentConfiguration<CbiGlobeConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController()
                .getAgentCommonConfiguration(CbiGlobeConfiguration.class);
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        if (authenticator == null) {
            authenticator =
                    new CbiGlobeAuthenticator(
                            apiClient,
                            new StrongAuthenticationState(request.getAppUriId()),
                            userState,
                            getAgentConfiguration().getClientConfiguration());
        }

        return authenticator;
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

    protected TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final CbiGlobeTransactionalAccountFetcher accountFetcher =
                CbiGlobeTransactionalAccountFetcher.createFromBoth(apiClient, persistentStorage);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(accountFetcher, 1)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {

        final SupplementalInformationProvider supplementalInformationProvider =
                new SupplementalInformationProviderImpl(supplementalRequester, request);

        CbiGlobePaymentExecutor paymentExecutor =
                new CbiGlobePaymentExecutor(
                        apiClient,
                        supplementalInformationProvider.getSupplementalInformationHelper(),
                        sessionStorage,
                        strongAuthenticationState);
        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(
                accounts, AccountIdentifier.Type.IBAN);
    }
}
